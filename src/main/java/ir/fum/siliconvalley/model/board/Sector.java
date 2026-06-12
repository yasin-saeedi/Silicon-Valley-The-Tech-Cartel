package ir.fum.siliconvalley.model.board;

import ir.fum.siliconvalley.model.enums.ResourceType;
import ir.fum.siliconvalley.model.enums.SectorType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public final class Sector implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final SectorPosition position;
    private final SectorType type;
    private final Integer activationNumber;
    private boolean audited;

    public Sector(SectorPosition position, SectorType type, Integer activationNumber) {
        this.position = Objects.requireNonNull(position, "position");
        this.type = Objects.requireNonNull(type, "type");
        if (type == SectorType.REGULATORY_ZONE && activationNumber != null) {
            throw new IllegalArgumentException("Regulatory zone cannot have an activation number");
        }
        if (type != SectorType.REGULATORY_ZONE) {
            if (activationNumber == null || activationNumber < 2 || activationNumber > 12 || activationNumber == 7) {
                throw new IllegalArgumentException("Productive sector activation number must be 2..12 excluding 7");
            }
        }
        this.activationNumber = activationNumber;
    }

    public SectorPosition getPosition() {
        return position;
    }

    public SectorType getType() {
        return type;
    }

    public Optional<ResourceType> getProducedResource() {
        return type.getProducedResource();
    }

    public OptionalInt getActivationNumber() {
        return activationNumber == null ? OptionalInt.empty() : OptionalInt.of(activationNumber);
    }

    public boolean isAudited() {
        return audited;
    }

    public void setAudited(boolean audited) {
        this.audited = audited;
    }
}
