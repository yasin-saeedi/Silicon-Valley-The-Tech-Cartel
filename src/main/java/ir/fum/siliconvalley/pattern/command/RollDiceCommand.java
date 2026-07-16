package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.game.GameEngine;

public final class RollDiceCommand implements GameCommand {

    private int number = 0;

    @Override
    public String description() {
        return number == 0 ? "Roll Two dice..." : "Two dice rolled: " + this.number ;
    }

    @Override
    public void execute(GameEngine engine) throws GameException {
        this.number = engine.rollDice();
    }
}
