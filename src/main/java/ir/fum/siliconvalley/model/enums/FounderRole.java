package ir.fum.siliconvalley.model.enums;

import ir.fum.siliconvalley.util.GameConstants;

public enum FounderRole {
    HACKER_CEO(1, 1, 0, 0, GameConstants.DEFAULT_TAX_HAND_LIMIT),
    TECH_GURU_CTO(1, 0, 1, 0, GameConstants.DEFAULT_TAX_HAND_LIMIT),
    VC_FUNDED(1, 0, 0, 2, GameConstants.VC_FUNDED_TAX_HAND_LIMIT);

    private final int victoryPointPenalty;
    private final int marketPriceDiscount;
    private final int unicornCloudDiscount;
    private final int startingCapitalBonus;
    private final int taxHandLimit;

    FounderRole(
            int victoryPointPenalty,
            int marketPriceDiscount,
            int unicornCloudDiscount,
            int startingCapitalBonus,
            int taxHandLimit
    ) {
        this.victoryPointPenalty = victoryPointPenalty;
        this.marketPriceDiscount = marketPriceDiscount;
        this.unicornCloudDiscount = unicornCloudDiscount;
        this.startingCapitalBonus = startingCapitalBonus;
        this.taxHandLimit = taxHandLimit;
    }

    public int getVictoryPointPenalty() {
        return victoryPointPenalty;
    }

    public int getMarketPriceDiscount() {
        return marketPriceDiscount;
    }

    public int getUnicornCloudDiscount() {
        return unicornCloudDiscount;
    }

    public int getStartingCapitalBonus() {
        return startingCapitalBonus;
    }

    public int getTaxHandLimit() {
        return taxHandLimit;
    }
}
