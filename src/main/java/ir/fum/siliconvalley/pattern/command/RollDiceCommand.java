package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.game.GameEngine;

public final class RollDiceCommand implements GameCommand {
    @Override
    public String description() {
        return "Roll two dice";
    }

    @Override
    public void execute(GameEngine engine) throws GameException {
        engine.rollDice();
    }
}
