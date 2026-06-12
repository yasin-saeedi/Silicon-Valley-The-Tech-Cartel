package ir.fum.siliconvalley.model.player;

import ir.fum.siliconvalley.exception.InsufficientResourcesException;
import ir.fum.siliconvalley.exception.InvalidGameActionException;
import ir.fum.siliconvalley.model.enums.FounderRole;
import ir.fum.siliconvalley.model.enums.PlayerColor;
import ir.fum.siliconvalley.model.enums.ResourceType;
import ir.fum.siliconvalley.model.resource.ResourceBundle;
import ir.fum.siliconvalley.model.structure.CompanyStructure;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class Player implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id = UUID.randomUUID();
    private final String name;
    private final PlayerColor color;
    private final EnumMap<ResourceType, Integer> resources = new EnumMap<>(ResourceType.class);
    private final List<CompanyStructure> structures = new ArrayList<>();
    private FounderRole founderRole;

    public Player(String name, PlayerColor color) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be blank");
        }
        this.name = name.trim();
        this.color = Objects.requireNonNull(color, "color");
        for (ResourceType type : ResourceType.values()) {
            resources.put(type, 0);
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlayerColor getColor() {
        return color;
    }

    public Optional<FounderRole> getFounderRole() {
        return Optional.ofNullable(founderRole);
    }

    public void assignFounderRole(FounderRole role) throws InvalidGameActionException {
        Objects.requireNonNull(role, "role");
        if (founderRole != null) {
            throw new InvalidGameActionException("Founder role has already been selected");
        }
        founderRole = role;
        if (role.getStartingCapitalBonus() > 0) {
            addResources(ResourceBundle.single(ResourceType.CAPITAL, role.getStartingCapitalBonus()));
        }
    }

    public int getResourceCount(ResourceType type) {
        return resources.getOrDefault(Objects.requireNonNull(type), 0);
    }

    public Map<ResourceType, Integer> getResourcesView() {
        return Collections.unmodifiableMap(resources);
    }

    public int getTotalResourceCards() {
        return resources.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void addResources(ResourceBundle resourceBundle) {
        for (ResourceType type : ResourceType.values()) {
            resources.merge(type, resourceBundle.get(type), Integer::sum);
        }
    }

    public boolean canAfford(ResourceBundle cost) {
        return cost.asMap().entrySet().stream()
                .allMatch(entry -> getResourceCount(entry.getKey()) >= entry.getValue());
    }

    public void spendResources(ResourceBundle cost) throws InsufficientResourcesException {
        if (!canAfford(cost)) {
            throw new InsufficientResourcesException(
                    "Player " + name + " cannot afford cost " + cost + "; inventory=" + resources
            );
        }
        for (ResourceType type : ResourceType.values()) {
            resources.merge(type, -cost.get(type), Integer::sum);
        }
    }

    public List<CompanyStructure> getStructures() {
        return Collections.unmodifiableList(structures);
    }

    public void addStructure(CompanyStructure structure) {
        Objects.requireNonNull(structure, "structure");
        if (!id.equals(structure.getOwnerId())) {
            throw new IllegalArgumentException("Structure owner does not match player");
        }
        structures.add(structure);
    }

    public void replaceStructure(UUID oldStructureId, CompanyStructure replacement)
            throws InvalidGameActionException {
        for (int index = 0; index < structures.size(); index++) {
            if (structures.get(index).getId().equals(oldStructureId)) {
                if (!id.equals(replacement.getOwnerId())) {
                    throw new InvalidGameActionException("Replacement structure owner does not match player");
                }
                structures.set(index, replacement);
                return;
            }
        }
        throw new InvalidGameActionException("Owned structure not found: " + oldStructureId);
    }

    public Optional<CompanyStructure> findStructure(UUID structureId) {
        return structures.stream().filter(item -> item.getId().equals(structureId)).findFirst();
    }

    public int calculateBaseVictoryPoints() {
        int structurePoints = structures.stream().mapToInt(CompanyStructure::getVictoryPoints).sum();
        int rolePenalty = founderRole == null ? 0 : founderRole.getVictoryPointPenalty();
        return structurePoints - rolePenalty;
    }

    public int getTaxHandLimit() {
        return founderRole == null ? 7 : founderRole.getTaxHandLimit();
    }
}
