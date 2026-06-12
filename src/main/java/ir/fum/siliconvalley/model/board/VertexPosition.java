package ir.fum.siliconvalley.model.board;

import java.io.Serializable;

public record VertexPosition(int row, int column) implements Serializable {
    public VertexPosition {
        if (row < 0 || column < 0) {
            throw new IllegalArgumentException("Vertex coordinates must be non-negative");
        }
    }

    public boolean isOrthogonallyAdjacent(VertexPosition other) {
        return Math.abs(row - other.row) + Math.abs(column - other.column) == 1;
    }
}
