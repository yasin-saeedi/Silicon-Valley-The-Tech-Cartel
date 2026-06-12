package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.game.GameEngine;

public interface GameCommand {
    String description();

    void execute(GameEngine engine) throws GameException;
}
