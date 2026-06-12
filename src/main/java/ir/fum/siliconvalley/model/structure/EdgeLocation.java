package ir.fum.siliconvalley.model.structure;

import ir.fum.siliconvalley.model.board.EdgePosition;

import java.util.Objects;

public record EdgeLocation(EdgePosition position) implements StructureLocation {
    public EdgeLocation {
        Objects.requireNonNull(position, "position");
    }
}
