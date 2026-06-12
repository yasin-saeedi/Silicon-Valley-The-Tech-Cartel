package ir.fum.siliconvalley.model.structure;

import ir.fum.siliconvalley.model.board.VertexPosition;

import java.util.Objects;

public record VertexLocation(VertexPosition position) implements StructureLocation {
    public VertexLocation {
        Objects.requireNonNull(position, "position");
    }
}
