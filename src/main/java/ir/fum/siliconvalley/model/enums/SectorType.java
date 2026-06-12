package ir.fum.siliconvalley.model.enums;

import java.util.Optional;

public enum SectorType {
    AI_HUB(ResourceType.TALENT),
    FINTECH_DISTRICT(ResourceType.CAPITAL),
    CLOUD_CAMPUS(ResourceType.CLOUD),
    IP_QUARTER(ResourceType.PATENT),
    DATA_VALLEY(ResourceType.DATA),
    REGULATORY_ZONE(null);

    private final ResourceType producedResource;

    SectorType(ResourceType producedResource) {
        this.producedResource = producedResource;
    }

    public Optional<ResourceType> getProducedResource() {
        return Optional.ofNullable(producedResource);
    }

    public boolean producesResource() {
        return producedResource != null;
    }
}
