package ir.fum.siliconvalley.model.game;

import ir.fum.siliconvalley.model.board.Board;
import ir.fum.siliconvalley.model.enums.GamePhase;
import ir.fum.siliconvalley.model.enums.TurnStage;
import ir.fum.siliconvalley.model.market.Market;
import ir.fum.siliconvalley.model.player.Player;
import ir.fum.siliconvalley.util.GameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;

/** Serializable aggregate root for one complete game state. */
public final class Game implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id = UUID.randomUUID();
    private final Board board;
    private final Market market;
    private final List<Player> players;
    private GamePhase phase = GamePhase.SETUP;
    private TurnStage turnStage = TurnStage.SETUP_PLACEMENT;
    private int currentPlayerIndex;
    private int fullRoundNumber = 1;
    private Integer lastDiceTotal;
    private long nextStructureSequence = 1;
    private int setupPlacementCount = 0;
    private final Set<UUID> playersPendingDiscard = new java.util.HashSet<>();
    private UUID longestNetworkOwnerId;
    private int longestNetworkLength;

    public Game(Board board, Market market, List<Player> players) {
        this.board = Objects.requireNonNull(board, "board");
        this.market = Objects.requireNonNull(market, "market");
        if (players == null || players.size() < GameConstants.MIN_PLAYERS || players.size() > GameConstants.MAX_PLAYERS) {
            throw new IllegalArgumentException("Game requires 2 to 4 players");
        }
        this.players = new ArrayList<>(players);
    }

    public UUID getId() {
        return id;
    }

    public Board getBoard() {
        return board;
    }

    public Market getMarket() {
        return market;
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public Optional<Player> findPlayer(UUID playerId) {
        return players.stream().filter(player -> player.getId().equals(playerId)).findFirst();
    }

    public Optional<ir.fum.siliconvalley.model.structure.CompanyStructure> findStructure(UUID structureId) {
        return players.stream()
                .flatMap(player -> player.getStructures().stream())
                .filter(structure -> structure.getId().equals(structureId))
                .findFirst();
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = Objects.requireNonNull(phase, "phase");
    }

    public TurnStage getTurnStage() {
        return turnStage;
    }

    public void setTurnStage(TurnStage turnStage) {
        this.turnStage = Objects.requireNonNull(turnStage, "turnStage");
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void moveToNextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        if (currentPlayerIndex < 0 || currentPlayerIndex >= players.size()) {
            throw new IllegalArgumentException("Player index out of bounds");
        }
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public int getFullRoundNumber() {
        return fullRoundNumber;
    }

    public void incrementFullRoundNumber() {
        fullRoundNumber++;
    }

    public OptionalInt getLastDiceTotal() {
        return lastDiceTotal == null ? OptionalInt.empty() : OptionalInt.of(lastDiceTotal);
    }

    public void setLastDiceTotal(int lastDiceTotal) {
        this.lastDiceTotal = lastDiceTotal;
    }

    public long nextStructureSequence() {
        return nextStructureSequence++;
    }

    public int getSetupPlacementCount() {
        return setupPlacementCount;
    }

    public void incrementSetupPlacementCount() {
        setupPlacementCount++;
    }

    public Set<UUID> getPlayersPendingDiscard() {
        return playersPendingDiscard;
    }

    public Optional<UUID> getLongestNetworkOwnerId() {
        return Optional.ofNullable(longestNetworkOwnerId);
    }

    public int getLongestNetworkLength() {
        return longestNetworkLength;
    }

    public void updateLongestNetwork(UUID ownerId, int length) {
        this.longestNetworkOwnerId = ownerId;
        this.longestNetworkLength = length;
    }
}
