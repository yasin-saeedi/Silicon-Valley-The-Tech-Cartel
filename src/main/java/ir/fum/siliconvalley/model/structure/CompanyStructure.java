package ir.fum.siliconvalley.model.structure;

import ir.fum.siliconvalley.model.enums.StructureType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public abstract class CompanyStructure implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID ownerId;
    private final StructureLocation location;
    private final long creationSequence;

    protected CompanyStructure(UUID ownerId, StructureLocation location, long creationSequence) {
        this(UUID.randomUUID(), ownerId, location, creationSequence);
    }

    protected CompanyStructure(UUID id, UUID ownerId, StructureLocation location, long creationSequence) {
        this.id = Objects.requireNonNull(id, "id");
        this.ownerId = Objects.requireNonNull(ownerId, "ownerId");
        this.location = Objects.requireNonNull(location, "location");
        this.creationSequence = creationSequence;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public StructureLocation getLocation() {
        return location;
    }

    public long getCreationSequence() {
        return creationSequence;
    }

    /** Units generated when one adjacent productive sector activates. */
    public abstract int produce();

    public abstract int getVictoryPoints();

    public abstract StructureType getStructureType();
}
