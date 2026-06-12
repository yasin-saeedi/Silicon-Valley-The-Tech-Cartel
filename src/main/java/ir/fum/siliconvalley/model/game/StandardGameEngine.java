package ir.fum.siliconvalley.model.game;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.exception.InvalidGameActionException;
import ir.fum.siliconvalley.model.board.Board;
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

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Baseline engine. Phase 1 implements the low-conflict core flow and deliberately
 * leaves complex spatial rules as explicit Phase 2 tasks.
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
            game.setTurnStage(TurnStage.TAX_DISCARD);
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
    public void placeInitialMvpAndPartnership(UUID playerId, VertexPosition vertex, EdgePosition edge) {
        throw phaseTwo("Initial-placement snake order and spatial validation");
    }

    @Override
    public void buildMvp(UUID playerId, VertexPosition vertex) {
        throw phaseTwo("Paid MVP build and distance-of-two validation");
    }

    @Override
    public void buildPartnership(UUID playerId, EdgePosition edge) {
        throw phaseTwo("Paid Partnership build and connected-network validation");
    }

    @Override
    public void upgradeMvpToUnicorn(UUID playerId, UUID mvpId) {
        throw phaseTwo("MVP-to-Unicorn upgrade workflow");
    }

    @Override
    public void buyFromMarket(UUID playerId, ResourceType resourceType) throws GameException {
        requireActionPlayer(playerId);
        game.getMarket().purchase(game.getCurrentPlayer(), resourceType);
    }

    @Override
    public void moveAuditor(UUID playerId, SectorPosition sectorPosition) throws GameException {
        Player currentPlayer = requireCurrentPlayer(playerId);
        if (game.getTurnStage() != TurnStage.AUDITOR_MOVE && game.getTurnStage() != TurnStage.TAX_DISCARD) {
            throw new InvalidGameActionException("Auditor can only move during a regulatory crisis");
        }
        // Phase 2 adds the written restriction requiring a sector adjacent to at least one company when possible.
        game.getBoard().setAuditorPosition(sectorPosition);
        game.setTurnStage(TurnStage.ACTIONS);
    }

    @Override
    public void finishTurn() throws GameException {
        requireStage(TurnStage.ACTIONS);
        int previousIndex = game.getCurrentPlayerIndex();
        game.moveToNextPlayer();
        if (game.getCurrentPlayerIndex() <= previousIndex) {
            game.getMarket().closeFullRound();
            game.incrementFullRoundNumber();
        }
        game.setTurnStage(TurnStage.ROLL_DICE);
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

    private UnsupportedOperationException phaseTwo(String feature) {
        return new UnsupportedOperationException(feature + " is intentionally scheduled for Phase 2");
    }
}
