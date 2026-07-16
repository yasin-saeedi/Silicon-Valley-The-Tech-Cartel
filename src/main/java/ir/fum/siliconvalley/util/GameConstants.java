package ir.fum.siliconvalley.util;

import ir.fum.siliconvalley.model.enums.ResourceType;
import ir.fum.siliconvalley.model.resource.ResourceBundle;

public final class GameConstants {
    public static final int DEFAULT_BOARD_SIZE = 5;
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 4;
    public static final int WINNING_VICTORY_POINTS = 10;
    public static final int DEFAULT_TAX_HAND_LIMIT = 7;
    public static final int VC_FUNDED_TAX_HAND_LIMIT = 9;
    public static final int LONGEST_NETWORK_MINIMUM_LENGTH = 3;
    public static final int LONGEST_NETWORK_BONUS = 2;
    public static final int BASE_MARKET_PRICE = 4;
    public static final int MIN_MARKET_PRICE = 2;
    public static final int MAX_MARKET_PRICE = 6;
    public static final int MARKET_PRICE_DECAY_ROUNDS = 3;

    public static final double CELL_WIDTH = 118;
    public static final double CELL_HEIGHT = 88;
    public static final double EDGE_THICKNESS = 5;
    public static final double VERTEX_RADIUS = 6;
    public static final double WINDOW_WIDTH = 1120;
    public static final double WINDOW_HEIGHT = 820;

    public static final ResourceBundle MVP_COST = ResourceBundle.builder()
            .add(ResourceType.CAPITAL, 1)
            .add(ResourceType.TALENT, 1)
            .add(ResourceType.CLOUD, 1)
            .add(ResourceType.DATA, 1)
            .build();

    public static final ResourceBundle UNICORN_UPGRADE_COST = ResourceBundle.builder()
            .add(ResourceType.CLOUD, 2)
            .add(ResourceType.DATA, 3)
            .build();

    public static final ResourceBundle PARTNERSHIP_COST = ResourceBundle.builder()
            .add(ResourceType.CAPITAL, 1)
            .add(ResourceType.PATENT, 1)
            .build();

    private GameConstants() {
    }
}
