package ir.fum.siliconvalley.view.board;

import ir.fum.siliconvalley.model.enums.PlayerColor;
import ir.fum.siliconvalley.model.enums.StructureType;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/** Loads and caches structure (MVP / Unicorn) per player color */
final class StructureDisplay {

    private static final Map<StructureType, Map<PlayerColor, Image>> CACHE = new EnumMap<>(StructureType.class);

    private StructureDisplay() {
    }

    public static Image icon(StructureType type, PlayerColor color) {
        return CACHE
                .computeIfAbsent(type, t -> new EnumMap<>(PlayerColor.class))
                .computeIfAbsent(color, c -> loadImage(type, c));
    }

    private static Image loadImage(StructureType type, PlayerColor color) {
        String colorName = switch (color) {
            case RED -> "Red";
            case BLUE -> "Blue";
            case GREEN -> "Green";
            case YELLOW -> "Yellow";
        };
        String typeName = switch (type) {
            case MVP -> "MVP";
            case UNICORN -> "Unicorn";
            case PARTNERSHIP -> throw new IllegalArgumentException("No artwork for PARTNERSHIP");
        };

        String path = "/Images/" + colorName + " " + typeName + ".png";
        InputStream stream = StructureDisplay.class.getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException("Missing structure image resource: " + path);
        }
        return new Image(stream);
    }
}