package ir.fum.siliconvalley.model.game;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.board.EdgePosition;
import ir.fum.siliconvalley.model.board.SectorPosition;
import ir.fum.siliconvalley.model.board.VertexPosition;
import ir.fum.siliconvalley.model.enums.ResourceType;

import java.util.UUID;

/** Single mutation boundary for gameplay rules. */
public interface GameEngine {
    Game getGame();

    void restore(Game game);

    void startMainPhase();

    int rollDice() throws GameException;

    void placeInitialMvpAndPartnership(UUID playerId, VertexPosition vertex, EdgePosition edge) throws GameException;

    void buildMvp(UUID playerId, VertexPosition vertex) throws GameException;

    void buildPartnership(UUID playerId, EdgePosition edge) throws GameException;

    void upgradeMvpToUnicorn(UUID playerId, UUID mvpId) throws GameException;

    void buyFromMarket(UUID playerId, ResourceType resourceType) throws GameException;

    void moveAuditor(UUID playerId, SectorPosition sectorPosition) throws GameException;

    void finishTurn() throws GameException;
}
