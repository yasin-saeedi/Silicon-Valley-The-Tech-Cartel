package ir.fum.siliconvalley.model.market;

import ir.fum.siliconvalley.exception.InsufficientResourcesException;
import ir.fum.siliconvalley.model.enums.FounderRole;
import ir.fum.siliconvalley.model.enums.ResourceType;
import ir.fum.siliconvalley.model.player.Player;
import ir.fum.siliconvalley.model.resource.ResourceBundle;
import ir.fum.siliconvalley.util.GameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class Market implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final EnumMap<ResourceType, Integer> prices = new EnumMap<>(ResourceType.class);
    private final EnumMap<ResourceType, Integer> inactiveRounds = new EnumMap<>(ResourceType.class);
    private final EnumSet<ResourceType> purchasedThisRound = EnumSet.noneOf(ResourceType.class);

    public Market() {
        for (ResourceType type : ResourceType.values()) {
            if (type.equals(ResourceType.CAPITAL))
                continue;
            prices.put(type, GameConstants.BASE_MARKET_PRICE);
            inactiveRounds.put(type, 0);
        }
    }

    public int getVisiblePrice(ResourceType type) {
        return prices.get(Objects.requireNonNull(type));
    }

    public int getEffectivePrice(ResourceType type, Optional<FounderRole> role) {
        int discount = role.map(FounderRole::getMarketPriceDiscount).orElse(0);
        return Math.max(GameConstants.MIN_MARKET_PRICE, getVisiblePrice(type) - discount);
    }

    public Map<ResourceType, Integer> getPricesView() {
        return Collections.unmodifiableMap(prices);
    }

    public void purchase(Player buyer, ResourceType type) throws InsufficientResourcesException {
        Objects.requireNonNull(buyer, "buyer");
        Objects.requireNonNull(type, "type");
        int price = getEffectivePrice(type, buyer.getFounderRole());
        buyer.spendResources(ResourceBundle.single(ResourceType.CAPITAL, price));
        buyer.addResources(ResourceBundle.single(type, 1));
        purchasedThisRound.add(type);
        prices.compute(type, (ignored, current) -> Math.min(GameConstants.MAX_MARKET_PRICE, current + 1));
    }

    /** Apply price decay once after every player has completed a turn. */
    public void closeFullRound() {
        for (ResourceType type : ResourceType.values()) {
            if (type.equals(ResourceType.CAPITAL))
                continue;
            if (purchasedThisRound.contains(type)) {
                inactiveRounds.put(type, 0);
                continue;
            }
            int newInactiveRounds = inactiveRounds.merge(type, 1, Integer::sum);
            if (newInactiveRounds >= GameConstants.MARKET_PRICE_DECAY_ROUNDS) {
                prices.compute(type, (ignored, current) -> Math.max(GameConstants.MIN_MARKET_PRICE, current - 1));
                inactiveRounds.put(type, 0);
            }
        }
        purchasedThisRound.clear();
    }
}
