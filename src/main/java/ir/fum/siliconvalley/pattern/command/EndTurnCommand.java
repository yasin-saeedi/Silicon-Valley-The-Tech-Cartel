package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.game.GameEngine;

public final class EndTurnCommand implements GameCommand {
    @Override
    public String description() {
        return "Finish current turn";
    }

    @Override
    public void execute(GameEngine engine) throws GameException {
        engine.finishTurn();
    }
}
