package ir.fum.siliconvalley.model.structure;

import ir.fum.siliconvalley.model.enums.StructureType;

import java.util.UUID;

public final class Partnership extends CompanyStructure {
    public Partnership(UUID ownerId, EdgeLocation location, long creationSequence) {
        super(ownerId, location, creationSequence);
    }

    @Override
    public int produce() {
        return 0;
    }

    @Override
    public int getVictoryPoints() {
        return 0;
    }

    @Override
    public StructureType getStructureType() {
        return StructureType.PARTNERSHIP;
    }
}
