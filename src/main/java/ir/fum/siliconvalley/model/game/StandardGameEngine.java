package ir.fum.siliconvalley.model.game;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.exception.InsufficientResourcesException;
import ir.fum.siliconvalley.exception.InvalidGameActionException;
import ir.fum.siliconvalley.exception.InvalidPlacementException;
import ir.fum.siliconvalley.model.board.Board;
import ir.fum.siliconvalley.model.board.Edge;
import ir.fum.siliconvalley.model.board.EdgePosition;
import ir.fum.siliconvalley.model.board.Sector;
import ir.fum.siliconvalley.model.board.SectorPosition;
import ir.fum.siliconvalley.model.board.Vertex;
import ir.fum.siliconvalley.model.board.VertexPosition;
import ir.fum.siliconvalley.model.enums.GamePhase;
import ir.fum.siliconvalley.model.enums.ResourceType;
import ir.fum.siliconvalley.model.enums.TurnStage;
import ir.fum.siliconvalley.model.player.Player;
import ir.fum.siliconvalley.model.resource.ResourceBundle;
import ir.fum.siliconvalley.model.structure.CompanyStructure;
import ir.fum.siliconvalley.model.structure.MVP;
import ir.fum.siliconvalley.model.structure.Partnership;
import ir.fum.siliconvalley.model.structure.Unicorn;
import ir.fum.siliconvalley.model.structure.VertexLocation;
import ir.fum.siliconvalley.model.structure.EdgeLocation;
import ir.fum.siliconvalley.util.GameConstants;

import java.util.List;
import java.util.ArrayList;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Standard game engine implementing all core gameplay rules, validation, and victory condition checking.
 */
public final class StandardGameEngine implements GameEngine {
    private Game game;
    private final Random random;

    public StandardGameEngine(Game game) {
        this(game, new Random());
    }

    public StandardGameEngine(Game game, Random random) {
        this.game = Objects.requireNonNull(game, "game");
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public Game getGame() {
        return game;
    }

    @Override
    public void restore(Game game) {
        this.game = Objects.requireNonNull(game, "game");
    }

    @Override
    public void startMainPhase() {
        game.setPhase(GamePhase.MAIN_TURN);
        game.setTurnStage(TurnStage.ROLL_DICE);
    }

    @Override
    public int rollDice() throws InvalidGameActionException {
        requireStage(TurnStage.ROLL_DICE);
        int total = random.nextInt(6) + 1 + random.nextInt(6) + 1;
        game.setLastDiceTotal(total);
        if (total == 7) {
            game.getPlayersPendingDiscard().clear();
            for (Player p : game.getPlayers()) {
                if (p.getTotalResourceCards() > p.getTaxHandLimit()) {
                    game.getPlayersPendingDiscard().add(p.getId());
                }
            }
            if (!game.getPlayersPendingDiscard().isEmpty()) {
                game.setTurnStage(TurnStage.TAX_DISCARD);
            } else {
                game.setTurnStage(TurnStage.AUDITOR_MOVE);
            }
        } else {
            produceResources(total);
            game.setTurnStage(TurnStage.ACTIONS);
        }
        return total;
    }

    private void produceResources(int activationNumber) {
        Board board = game.getBoard();
        for (Sector sector : board.getSectors()) {
            if (sector.isAudited() || sector.getActivationNumber().isEmpty()
                    || sector.getActivationNumber().getAsInt() != activationNumber) {
                continue;
            }
            Optional<ResourceType> resourceType = sector.getProducedResource();
            if (resourceType.isEmpty()) {
                continue;
            }
            for (Vertex vertex : board.getAdjacentVertices(sector.getPosition())) {
                vertex.getCompanyStructureId()
                        .flatMap(game::findStructure)
                        .ifPresent(structure -> grantProduction(structure, resourceType.orElseThrow()));
            }
        }
    }

    private void grantProduction(CompanyStructure structure, ResourceType resourceType) {
        if (structure.produce() <= 0) {
            return;
        }
        game.findPlayer(structure.getOwnerId())
                .orElseThrow(() -> new IllegalStateException("Structure owner is missing"))
                .addResources(ResourceBundle.single(resourceType, structure.produce()));
    }

    @Override
    public void placeInitialMvpAndPartnership(UUID playerId, VertexPosition vertex, EdgePosition edge) throws GameException {
        if (game.getPhase() != GamePhase.SETUP || game.getTurnStage() != TurnStage.SETUP_PLACEMENT) {
            throw new InvalidGameActionException("Initial placement is only allowed during the setup phase");
        }

        List<Player> players = game.getPlayers();
        int n = players.size();
        int count = game.getSetupPlacementCount();
        int activeIndex;
        if (count < n) {
            activeIndex = count;
        } else if (count < 2 * n) {
            activeIndex = 2 * n - 1 - count;
        } else {
            throw new InvalidGameActionException("Setup placements are already complete");
        }

        Player activePlayer = players.get(activeIndex);
        if (!activePlayer.getId().equals(playerId)) {
            throw new InvalidGameActionException("It is player " + activePlayer.getName() + "'s turn to place");
        }

        Board board = game.getBoard();

        // Target vertex and edge must be free
        if (board.getVertex(vertex).getCompanyStructureId().isPresent()) {
            throw new InvalidPlacementException("Vertex is already occupied: " + vertex);
        }
        if (board.getEdge(edge).getPartnershipId().isPresent()) {
            throw new InvalidPlacementException("Edge is already occupied: " + edge);
        }

        // The edge must touch the vertex
        if (!edge.touches(vertex)) {
            throw new InvalidPlacementException("The placed Partnership must connect to the placed MVP");
        }

        // Distance of two rule
        if (!board.isDistanceOfTwoValid(vertex)) {
            throw new InvalidPlacementException("Distance-of-two rule violated at vertex: " + vertex);
        }

        // Place MVP
        long seqMvp = game.nextStructureSequence();
        MVP mvp = new MVP(playerId, new VertexLocation(vertex), seqMvp);
        activePlayer.addStructure(mvp);
        board.placeCompany(mvp.getId(), vertex);

        // Place Partnership
        long seqPartnership = game.nextStructureSequence();
        Partnership partnership = new Partnership(playerId, new EdgeLocation(edge), seqPartnership);
        activePlayer.addStructure(partnership);
        board.placePartnership(partnership.getId(), edge);

        // Increment count
        game.incrementSetupPlacementCount();
        int newCount = game.getSetupPlacementCount();

        if (newCount < 2 * n) {
            // Update active player index in setup snake order
            int nextActiveIndex;
            if (newCount < n) {
                nextActiveIndex = newCount;
            } else {
                nextActiveIndex = 2 * n - 1 - newCount;
            }
            game.setCurrentPlayerIndex(nextActiveIndex);
        } else {
            // Setup phase finished!
            // Distribute initial resources from adjacent sectors of the second MVP
            for (Player player : players) {
                List<MVP> mvps = player.getStructures().stream()
                        .filter(s -> s instanceof MVP)
                        .map(s -> (MVP) s)
                        .sorted(java.util.Comparator.comparingLong(CompanyStructure::getCreationSequence))
                        .toList();
                if (mvps.size() >= 2) {
                    MVP secondMvp = mvps.get(1);
                    VertexLocation loc = (VertexLocation) secondMvp.getLocation();
                    for (Sector sector : board.getAdjacentSectors(loc.position())) {
                        if (!sector.getType().equals(ir.fum.siliconvalley.model.enums.SectorType.REGULATORY_ZONE)) {
                            sector.getProducedResource().ifPresent(res -> {
                                player.addResources(ResourceBundle.single(res, 1));
                            });
                        }
                    }
                }
            }

            // Transition game phase
            game.setPhase(GamePhase.MAIN_TURN);
            game.setTurnStage(TurnStage.ROLL_DICE);
            game.setCurrentPlayerIndex(0);
        }
    }

    @Override
    public void buildMvp(UUID playerId, VertexPosition vertex) throws GameException {
        Player player = requireActionPlayer(playerId);
        Board board = game.getBoard();

        // 1. Validation: Position unoccupied
        if (board.getVertex(vertex).getCompanyStructureId().isPresent()) {
            throw new InvalidPlacementException("Vertex is already occupied: " + vertex);
        }

        // 2. Validation: Distance of two rule
        if (!board.isDistanceOfTwoValid(vertex)) {
            throw new InvalidPlacementException("Distance-of-two rule violated at vertex: " + vertex);
        }

        // 3. Validation: Connected to player's existing network of Partnerships
        Set<UUID> playerStructureIds = player.getStructures().stream()
                .map(CompanyStructure::getId)
                .collect(Collectors.toSet());
        if (!board.isVertexConnectedToPlayerNetwork(vertex, playerStructureIds)) {
            throw new InvalidPlacementException("Vertex must be connected to player's existing Partnership network: " + vertex);
        }

        // 4. Validate resources and spend
        player.spendResources(GameConstants.MVP_COST);

        // 5. Place and register structure
        long seq = game.nextStructureSequence();
        MVP mvp = new MVP(playerId, new VertexLocation(vertex), seq);
        player.addStructure(mvp);
        board.placeCompany(mvp.getId(), vertex);
        checkVictoryCondition();
    }

    @Override
    public void buildPartnership(UUID playerId, EdgePosition edge) throws GameException {
        Player player = requireActionPlayer(playerId);
        Board board = game.getBoard();

        // 1. Validation: Position unoccupied
        if (board.getEdge(edge).getPartnershipId().isPresent()) {
            throw new InvalidPlacementException("Edge is already occupied: " + edge);
        }

        // 2. Validation: Connected to player's existing network of Partnerships or MVPs/Unicorns
        Set<UUID> playerStructureIds = player.getStructures().stream()
                .map(CompanyStructure::getId)
                .collect(Collectors.toSet());
        if (!board.isEdgeConnectedToPlayerNetwork(edge, playerStructureIds)) {
            throw new InvalidPlacementException("Edge must be connected to player's existing network: " + edge);
        }

        // 3. Validate resources and spend
        player.spendResources(GameConstants.PARTNERSHIP_COST);

        // 4. Place and register structure
        long seq = game.nextStructureSequence();
        Partnership partnership = new Partnership(playerId, new EdgeLocation(edge), seq);
        player.addStructure(partnership);
        board.placePartnership(partnership.getId(), edge);

        // 5. Recalculate longest network
        recalculateLongestNetwork();
        checkVictoryCondition();
    }

    @Override
    public void upgradeMvpToUnicorn(UUID playerId, UUID mvpId) throws GameException {
        Player player = requireActionPlayer(playerId);

        CompanyStructure structure = player.findStructure(mvpId)
                .orElseThrow(() -> new InvalidGameActionException("Player does not own structure: " + mvpId));

        if (structure.getStructureType() != ir.fum.siliconvalley.model.enums.StructureType.MVP) {
            throw new InvalidGameActionException("Structure is not an MVP: " + mvpId);
        }

        VertexLocation location = (VertexLocation) structure.getLocation();
        VertexPosition vertexPos = location.position();

        ResourceBundle cost = player.getFounderRole().map(r -> r.getUnicornCloudDiscount() > 0).orElse(false)
                ? ResourceBundle.builder().add(ResourceType.CLOUD, 1).add(ResourceType.DATA, 3).build()
                : GameConstants.UNICORN_UPGRADE_COST;

        player.spendResources(cost);

        // Replace structure
        long seq = game.nextStructureSequence();
        Unicorn unicorn = new Unicorn(playerId, location, seq);
        player.replaceStructure(mvpId, unicorn);

        game.getBoard().replaceCompany(mvpId, unicorn.getId(), vertexPos);
        checkVictoryCondition();
    }

    @Override
    public void buyFromMarket(UUID playerId, ResourceType resourceType) throws GameException {
        requireActionPlayer(playerId);
        game.getMarket().purchase(game.getCurrentPlayer(), resourceType);
    }

    @Override
    public void discardTaxes(UUID playerId, ResourceBundle resources) throws GameException {
        if (game.getPhase() != GamePhase.MAIN_TURN || game.getTurnStage() != TurnStage.TAX_DISCARD) {
            throw new InvalidGameActionException("Tax discarding is only allowed during the tax discard stage");
        }
        if (!game.getPlayersPendingDiscard().contains(playerId)) {
            throw new InvalidGameActionException("Player does not need to discard resources");
        }
        Player player = game.findPlayer(playerId)
                .orElseThrow(() -> new InvalidGameActionException("Player not found"));

        int totalCards = player.getTotalResourceCards();
        int requiredDiscard = totalCards / 2;
        if (resources.totalCards() != requiredDiscard) {
            throw new InvalidGameActionException("Player must discard exactly " + requiredDiscard + " resources");
        }

        player.spendResources(resources);
        game.getPlayersPendingDiscard().remove(playerId);

        if (game.getPlayersPendingDiscard().isEmpty()) {
            game.setTurnStage(TurnStage.AUDITOR_MOVE);
        }
    }

    @Override
    public void moveAuditor(UUID playerId, SectorPosition sectorPosition) throws GameException {
        Player currentPlayer = requireCurrentPlayer(playerId);
        if (game.getTurnStage() != TurnStage.AUDITOR_MOVE && game.getTurnStage() != TurnStage.TAX_DISCARD) {
            throw new InvalidGameActionException("Auditor can only move during a regulatory crisis");
        }

        Board board = game.getBoard();

        // Validate auditor target location: must be adjacent to at least one company structure if possible
        boolean anySectorHasCompany = false;
        for (Sector sector : board.getSectors()) {
            for (Vertex vertex : board.getAdjacentVertices(sector.getPosition())) {
                if (vertex.getCompanyStructureId().isPresent()) {
                    anySectorHasCompany = true;
                    break;
                }
            }
            if (anySectorHasCompany) {
                break;
            }
        }

        if (anySectorHasCompany) {
            boolean targetHasCompany = false;
            for (Vertex vertex : board.getAdjacentVertices(sectorPosition)) {
                if (vertex.getCompanyStructureId().isPresent()) {
                    targetHasCompany = true;
                    break;
                }
            }
            if (!targetHasCompany) {
                throw new InvalidPlacementException("Auditor must be placed on a sector adjacent to at least one company");
            }
        }

        board.setAuditorPosition(sectorPosition);
        game.setTurnStage(TurnStage.ACTIONS);
    }

    @Override
    public void tradeWithPlayer(UUID initiatorId, UUID targetId, ResourceBundle offered, ResourceBundle requested) throws GameException {
        Objects.requireNonNull(initiatorId, "initiatorId");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(offered, "offered");
        Objects.requireNonNull(requested, "requested");
        requireStage(TurnStage.ACTIONS);
        requireCurrentPlayer(initiatorId);

        if (initiatorId.equals(targetId)) {
            throw new InvalidGameActionException("Cannot trade with yourself");
        }
        Player initiator = game.findPlayer(initiatorId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown initiator: " + initiatorId));
        Player target = game.findPlayer(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown target player: " + targetId));

        if (offered.totalCards() == 0 && requested.totalCards() == 0) {
            throw new InvalidGameActionException("Trade must involve at least one offered or requested resource");
        }
        if (!initiator.canAfford(offered)) {
            throw new InsufficientResourcesException("Initiator does not have sufficient resources to offer");
        }
        if (!target.canAfford(requested)) {
            throw new InsufficientResourcesException("Target player does not have sufficient resources to complete trade");
        }

        initiator.spendResources(offered);
        target.addResources(offered);
        target.spendResources(requested);
        initiator.addResources(requested);
        checkVictoryCondition();
    }

    @Override
    public void finishTurn() throws GameException {
        requireStage(TurnStage.ACTIONS);
        checkVictoryCondition();
        if (game.getPhase() == GamePhase.FINISHED) {
            return;
        }
        int previousIndex = game.getCurrentPlayerIndex();
        game.moveToNextPlayer();
        if (game.getCurrentPlayerIndex() <= previousIndex) {
            game.getMarket().closeFullRound();
            game.incrementFullRoundNumber();
        }
        game.setTurnStage(TurnStage.ROLL_DICE);
    }

    private void checkVictoryCondition() {
        if (game.getPhase() == GamePhase.FINISHED) {
            return;
        }
        Player activePlayer = game.getCurrentPlayer();
        if (getTotalVictoryPoints(activePlayer) >= GameConstants.WINNING_VICTORY_POINTS) {
            game.setPhase(GamePhase.FINISHED);
            game.setTurnStage(TurnStage.FINISHED);
            return;
        }
        for (Player player : game.getPlayers()) {
            if (getTotalVictoryPoints(player) >= GameConstants.WINNING_VICTORY_POINTS) {
                game.setPhase(GamePhase.FINISHED);
                game.setTurnStage(TurnStage.FINISHED);
                break;
            }
        }
    }

    private int getTotalVictoryPoints(Player player) {
        int baseVp = player.calculateBaseVictoryPoints();
        boolean hasLongest = game.getLongestNetworkOwnerId()
                .map(id -> id.equals(player.getId()))
                .orElse(false);
        return baseVp + (hasLongest ? GameConstants.LONGEST_NETWORK_BONUS : 0);
    }

    private void recalculateLongestNetwork() {
        List<Player> players = game.getPlayers();
        UUID currentOwner = game.getLongestNetworkOwnerId().orElse(null);
        int currentRecord = game.getLongestNetworkLength();

        UUID newOwner = currentOwner;
        int newRecord = currentRecord;

        java.util.Map<UUID, Integer> lengths = new java.util.HashMap<>();
        for (Player player : players) {
            lengths.put(player.getId(), calculateLongestPartnershipChain(player.getId()));
        }

        int currentOwnerLen = currentOwner == null ? 0 : lengths.getOrDefault(currentOwner, 0);

        UUID challenger = null;
        int maxChallengerLen = Math.max(2, currentOwnerLen); // Must be at least 3

        for (Player player : players) {
            if (player.getId().equals(currentOwner)) {
                continue;
            }
            int len = lengths.get(player.getId());
            if (len > maxChallengerLen) {
                challenger = player.getId();
                maxChallengerLen = len;
            }
        }

        if (challenger != null) {
            newOwner = challenger;
            newRecord = maxChallengerLen;
        } else if (currentOwner != null) {
            newRecord = currentOwnerLen;
            if (newRecord < 3) {
                newOwner = null;
                newRecord = 0;
            }
        }

        game.updateLongestNetwork(newOwner, newRecord);
    }

    private int calculateLongestPartnershipChain(UUID playerId) {
        Board board = game.getBoard();
        java.util.Map<VertexPosition, List<Edge>> adj = new java.util.HashMap<>();

        for (Edge edge : board.getEdges()) {
            if (edge.getPartnershipId().isPresent()) {
                UUID pId = edge.getPartnershipId().orElseThrow();
                Optional<CompanyStructure> optS = game.findStructure(pId);
                if (optS.isPresent() && optS.get().getOwnerId().equals(playerId)) {
                    adj.computeIfAbsent(edge.getPosition().first(), k -> new ArrayList<>()).add(edge);
                    adj.computeIfAbsent(edge.getPosition().second(), k -> new ArrayList<>()).add(edge);
                }
            }
        }

        int maxLen = 0;
        for (VertexPosition startVertex : adj.keySet()) {
            maxLen = Math.max(maxLen, dfsLongestPath(startVertex, adj, new java.util.HashSet<>(), playerId));
        }
        return maxLen;
    }

    private int dfsLongestPath(VertexPosition currentVertex, java.util.Map<VertexPosition, List<Edge>> adj, java.util.Set<UUID> visitedEdges, UUID playerId) {
        if (isVertexBlockedByOpponent(currentVertex, playerId)) {
            return 0;
        }
        List<Edge> edges = adj.getOrDefault(currentVertex, List.of());
        int best = 0;
        for (Edge edge : edges) {
            UUID edgeId = edge.getPartnershipId().orElse(null);
            if (edgeId != null && !visitedEdges.contains(edgeId)) {
                visitedEdges.add(edgeId);

                VertexPosition nextVertex = edge.getPosition().first().equals(currentVertex)
                        ? edge.getPosition().second()
                        : edge.getPosition().first();

                int length = 1 + dfsLongestPath(nextVertex, adj, visitedEdges, playerId);
                best = Math.max(best, length);

                visitedEdges.remove(edgeId);
            }
        }
        return best;
    }

    private boolean isVertexBlockedByOpponent(VertexPosition vertexPos, UUID playerId) {
        return game.getBoard().getVertex(vertexPos).getCompanyStructureId()
                .flatMap(game::findStructure)
                .map(s -> !s.getOwnerId().equals(playerId))
                .orElse(false);
    }

    private Player requireActionPlayer(UUID playerId) throws InvalidGameActionException {
        requireStage(TurnStage.ACTIONS);
        return requireCurrentPlayer(playerId);
    }

    private Player requireCurrentPlayer(UUID playerId) throws InvalidGameActionException {
        if (!game.getCurrentPlayer().getId().equals(playerId)) {
            throw new InvalidGameActionException("Action belongs to the current player only");
        }
        return game.getCurrentPlayer();
    }

    private void requireStage(TurnStage expected) throws InvalidGameActionException {
        if (game.getPhase() != GamePhase.MAIN_TURN || game.getTurnStage() != expected) {
            throw new InvalidGameActionException(
                    "Expected stage " + expected + " but game is " + game.getPhase() + "/" + game.getTurnStage()
            );
        }
    }
}
