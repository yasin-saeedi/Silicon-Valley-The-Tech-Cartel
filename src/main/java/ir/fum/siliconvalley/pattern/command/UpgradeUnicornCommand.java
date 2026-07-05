package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.game.GameEngine;

import java.util.Objects;
import java.util.UUID;

public record UpgradeUnicornCommand(UUID playerId, UUID mvpId) implements GameCommand {
    public UpgradeUnicornCommand {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(mvpId, "mvpId");
    }

    @Override
    public String description() {
        return "Upgrade MVP " + mvpId + " to Unicorn";
    }

    @Override
    public void execute(GameEngine engine) throws GameException {
        engine.upgradeMvpToUnicorn(playerId, mvpId);
    }
}
