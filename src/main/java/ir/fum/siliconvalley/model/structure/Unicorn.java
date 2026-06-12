package ir.fum.siliconvalley.model.structure;

import ir.fum.siliconvalley.model.enums.StructureType;

import java.util.UUID;

public final class Unicorn extends CompanyStructure {
    public Unicorn(UUID ownerId, VertexLocation location, long creationSequence) {
        super(ownerId, location, creationSequence);
    }

    @Override
    public int produce() {
        return 2;
    }

    @Override
    public int getVictoryPoints() {
        return 2;
    }

    @Override
    public StructureType getStructureType() {
        return StructureType.UNICORN;
    }
}
