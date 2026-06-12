package ir.fum.siliconvalley.model.board;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public final class Edge implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final EdgePosition position;
    private UUID partnershipId;

    public Edge(EdgePosition position) {
        this.position = position;
    }

    public EdgePosition getPosition() {
        return position;
    }

    public Optional<UUID> getPartnershipId() {
        return Optional.ofNullable(partnershipId);
    }

    void occupy(UUID partnershipId) {
        this.partnershipId = partnershipId;
    }

    void clear() {
        partnershipId = null;
    }
}
