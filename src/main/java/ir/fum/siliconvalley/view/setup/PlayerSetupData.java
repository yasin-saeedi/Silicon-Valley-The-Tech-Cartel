package ir.fum.siliconvalley.view.setup;

import ir.fum.siliconvalley.model.enums.FounderRole;
import ir.fum.siliconvalley.model.enums.PlayerColor;

/** Data collected for a single player during initial game setup. */
public record PlayerSetupData(String name, PlayerColor color, FounderRole role) {
}