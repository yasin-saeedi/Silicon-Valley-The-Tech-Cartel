package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.game.GameEngine;
import ir.fum.siliconvalley.model.resource.ResourceBundle;

import java.util.Objects;
import java.util.UUID;

public record TradeWithPlayerCommand(UUID initiatorId, UUID targetId, ResourceBundle offered, ResourceBundle requested) implements GameCommand {
    public TradeWithPlayerCommand {
        Objects.requireNonNull(initiatorId, "initiatorId");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(offered, "offered");
        Objects.requireNonNull(requested, "requested");
    }

    @Override
    public String description() {
        return "Trade between players";
    }

    @Override
    public void execute(GameEngine engine) throws GameException {
        engine.tradeWithPlayer(initiatorId, targetId, offered, requested);
    }
}
