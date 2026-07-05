package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.board.SectorPosition;
import ir.fum.siliconvalley.model.game.GameEngine;

import java.util.Objects;
import java.util.UUID;

public record MoveAuditorCommand(UUID playerId, SectorPosition sectorPosition) implements GameCommand {
    public MoveAuditorCommand {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(sectorPosition, "sectorPosition");
    }

    @Override
    public String description() {
        return "Move Auditor to sector " + sectorPosition;
    }

    @Override
    public void execute(GameEngine engine) throws GameException {
        engine.moveAuditor(playerId, sectorPosition);
    }
}
