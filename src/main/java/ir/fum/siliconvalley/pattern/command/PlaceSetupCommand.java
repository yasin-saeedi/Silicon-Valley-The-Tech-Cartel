package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.board.EdgePosition;
import ir.fum.siliconvalley.model.board.VertexPosition;
import ir.fum.siliconvalley.model.game.GameEngine;

import java.util.Objects;
import java.util.UUID;

public record PlaceSetupCommand(UUID playerId, VertexPosition vertex, EdgePosition edge) implements GameCommand {
    public PlaceSetupCommand {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(vertex, "vertex");
        Objects.requireNonNull(edge, "edge");
    }

    @Override
    public String description() {
        return "Initial setup placement of MVP and Partnership";
    }

    @Override
    public void execute(GameEngine engine) throws GameException {
        engine.placeInitialMvpAndPartnership(playerId, vertex, edge);
    }
}
