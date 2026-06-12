package ir.fum.siliconvalley.model.board;

import java.io.Serializable;

public record SectorPosition(int row, int column) implements Serializable {
    public SectorPosition {
        if (row < 0 || column < 0) {
            throw new IllegalArgumentException("Sector coordinates must be non-negative");
        }
    }
}
