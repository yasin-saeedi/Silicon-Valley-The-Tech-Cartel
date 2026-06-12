package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.enums.ResourceType;
import ir.fum.siliconvalley.model.game.GameEngine;

import java.util.Objects;
import java.util.UUID;

public record BuyFromMarketCommand(UUID playerId, ResourceType resourceType) implements GameCommand {
    public BuyFromMarketCommand {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(resourceType, "resourceType");
    }

    @Override
    public String description() {
        return "Buy " + resourceType + " from market";
    }

    @Override
    public void execute(GameEngine engine) throws GameException {
        engine.buyFromMarket(playerId, resourceType);
    }
}
