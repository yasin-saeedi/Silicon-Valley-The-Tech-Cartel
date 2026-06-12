package ir.fum.siliconvalley.model.resource;

import ir.fum.siliconvalley.model.enums.ResourceType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Immutable group of resource quantities, useful for costs and transfers. */
public final class ResourceBundle implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final EnumMap<ResourceType, Integer> amounts;

    private ResourceBundle(EnumMap<ResourceType, Integer> amounts) {
        this.amounts = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            int amount = amounts.getOrDefault(type, 0);
            if (amount < 0) {
                throw new IllegalArgumentException("Resource amount cannot be negative: " + type);
            }
            this.amounts.put(type, amount);
        }
    }

    public static ResourceBundle empty() {
        return new ResourceBundle(new EnumMap<>(ResourceType.class));
    }

    public static ResourceBundle single(ResourceType type, int amount) {
        return builder().add(type, amount).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public int get(ResourceType type) {
        return amounts.getOrDefault(Objects.requireNonNull(type), 0);
    }

    public int totalCards() {
        return amounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<ResourceType, Integer> asMap() {
        return Collections.unmodifiableMap(amounts);
    }

    @Override
    public String toString() {
        return amounts.toString();
    }

    public static final class Builder {
        private final EnumMap<ResourceType, Integer> amounts = new EnumMap<>(ResourceType.class);

        public Builder add(ResourceType type, int amount) {
            Objects.requireNonNull(type, "type");
            if (amount < 0) {
                throw new IllegalArgumentException("Resource amount cannot be negative");
            }
            amounts.merge(type, amount, Integer::sum);
            return this;
        }

        public ResourceBundle build() {
            return new ResourceBundle(amounts);
        }
    }
}
