package ir.fum.siliconvalley.model.board;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public final class Vertex implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final VertexPosition position;
    private UUID companyStructureId;

    public Vertex(VertexPosition position) {
        this.position = position;
    }

    public VertexPosition getPosition() {
        return position;
    }

    public Optional<UUID> getCompanyStructureId() {
        return Optional.ofNullable(companyStructureId);
    }

    void occupy(UUID structureId) {
        this.companyStructureId = structureId;
    }

    void clear() {
        this.companyStructureId = null;
    }
}
