package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.board.EdgePosition;
import ir.fum.siliconvalley.model.game.GameEngine;

import java.util.Objects;
import java.util.UUID;

public record BuildPartnershipCommand(UUID playerId, EdgePosition edge) implements GameCommand {
    public BuildPartnershipCommand {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(edge, "edge");
    }

    @Override
    public String description() {
        return "Build Partnership at " + edge;
    }

    @Override
    public void execute(GameEngine engine) throws GameException {
        engine.buildPartnership(playerId, edge);
    }
}
