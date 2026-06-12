package ir.fum.siliconvalley;

import ir.fum.siliconvalley.model.board.Board;
import ir.fum.siliconvalley.model.board.RandomBoardFactory;
import ir.fum.siliconvalley.model.enums.PlayerColor;
import ir.fum.siliconvalley.model.enums.ResourceType;
import ir.fum.siliconvalley.model.game.Game;
import ir.fum.siliconvalley.model.game.StandardGameEngine;
import ir.fum.siliconvalley.model.market.Market;
import ir.fum.siliconvalley.model.player.Player;
import ir.fum.siliconvalley.model.resource.ResourceBundle;
import ir.fum.siliconvalley.pattern.memento.GameMemento;
import ir.fum.siliconvalley.pattern.memento.GameSnapshotCodec;

import java.util.List;
import java.util.Random;

public final class ModelSmokeTest {
    private ModelSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        Board board = new RandomBoardFactory().create(5, new Random(42));
        require(board.getSectors().size() == 25, "Expected 25 sectors");
        require(board.getVertices().size() == 36, "Expected 36 vertices");
        require(board.getEdges().size() == 60, "Expected 60 edges");

        Player first = new Player("Ada", PlayerColor.BLUE);
        Player second = new Player("Linus", PlayerColor.RED);
        first.addResources(ResourceBundle.single(ResourceType.CAPITAL, 10));

        Game game = new Game(board, new Market(), List.of(first, second));
        StandardGameEngine engine = new StandardGameEngine(game, new Random(1));
        engine.startMainPhase();
        int diceTotal = engine.rollDice();
        require(diceTotal >= 2 && diceTotal <= 12, "Dice total out of range");

        if (diceTotal == 7) {
            engine.moveAuditor(first.getId(), board.getSectors().iterator().next().getPosition());
        }
        engine.buyFromMarket(first.getId(), ResourceType.DATA);
        require(first.getResourceCount(ResourceType.DATA) == 1, "Market purchase failed");
        require(game.getMarket().getVisiblePrice(ResourceType.DATA) == 5, "Dynamic price increase failed");

        GameSnapshotCodec codec = new GameSnapshotCodec();
        GameMemento snapshot = codec.capture(game);
        Game restored = codec.restore(snapshot);
        require(restored.getBoard().getSectors().size() == 25, "Memento restoration failed");

        System.out.println("Model smoke test passed.");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
