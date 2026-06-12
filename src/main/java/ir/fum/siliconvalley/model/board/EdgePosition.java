package ir.fum.siliconvalley.model.board;

import java.io.Serializable;
import java.util.Objects;

/** Normalized undirected edge between two orthogonally adjacent vertices. */
public record EdgePosition(VertexPosition first, VertexPosition second) implements Serializable {
    public EdgePosition {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        if (!first.isOrthogonallyAdjacent(second)) {
            throw new IllegalArgumentException("An edge must connect adjacent vertices");
        }
        if (compare(first, second) > 0) {
            VertexPosition temporary = first;
            first = second;
            second = temporary;
        }
    }

    private static int compare(VertexPosition left, VertexPosition right) {
        int rowComparison = Integer.compare(left.row(), right.row());
        return rowComparison != 0 ? rowComparison : Integer.compare(left.column(), right.column());
    }

    public boolean touches(VertexPosition vertexPosition) {
        return first.equals(vertexPosition) || second.equals(vertexPosition);
    }
}
