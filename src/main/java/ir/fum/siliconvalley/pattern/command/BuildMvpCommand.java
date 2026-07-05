package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.board.VertexPosition;
import ir.fum.siliconvalley.model.game.GameEngine;

import java.util.Objects;
import java.util.UUID;

public record BuildMvpCommand(UUID playerId, VertexPosition vertex) implements GameCommand {
    public BuildMvpCommand {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(vertex, "vertex");
    }

    @Override
    public String description() {
        return "Build MVP at " + vertex;
    }

    @Override
    public void execute(GameEngine engine) throws GameException {
        engine.buildMvp(playerId, vertex);
    }
}
