package ir.fum.siliconvalley.model.structure;

import ir.fum.siliconvalley.model.enums.StructureType;

import java.util.UUID;

public final class MVP extends CompanyStructure {
    public MVP(UUID ownerId, VertexLocation location, long creationSequence) {
        super(ownerId, location, creationSequence);
    }

    @Override
    public int produce() {
        return 1;
    }

    @Override
    public int getVictoryPoints() {
        return 1;
    }

    @Override
    public StructureType getStructureType() {
        return StructureType.MVP;
    }
}
