package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.game.GameEngine;

public final class RollDiceCommand implements GameCommand {

    private int number = 0;

    @Override
    public String description() {
        if (number > 0)
            return "Two dice rolled: " + this.number ;
        else
            return "Roll Two dice..." ;
    }

    @Override
    public void execute(GameEngine engine) throws GameException {
        this.number = engine.rollDice();
    }
}
