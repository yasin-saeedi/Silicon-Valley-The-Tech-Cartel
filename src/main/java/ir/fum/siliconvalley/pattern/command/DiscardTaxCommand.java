package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.game.GameEngine;
import ir.fum.siliconvalley.model.resource.ResourceBundle;

import java.util.Objects;
import java.util.UUID;

public record DiscardTaxCommand(UUID playerId, ResourceBundle resources) implements GameCommand {
    public DiscardTaxCommand {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(resources, "resources");
    }

    @Override
    public String description() {
        return "Discard tax resources: " + resources;
    }

    @Override
    public void execute(GameEngine engine) throws GameException {
        engine.discardTaxes(playerId, resources);
    }
}
