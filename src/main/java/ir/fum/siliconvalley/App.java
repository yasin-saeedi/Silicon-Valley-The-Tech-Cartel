package ir.fum.siliconvalley;

import ir.fum.siliconvalley.controller.GameController;
import ir.fum.siliconvalley.model.board.Board;
import ir.fum.siliconvalley.model.board.RandomBoardFactory;
import ir.fum.siliconvalley.model.enums.PlayerColor;
import ir.fum.siliconvalley.model.game.Game;
import ir.fum.siliconvalley.model.game.StandardGameEngine;
import ir.fum.siliconvalley.model.market.Market;
import ir.fum.siliconvalley.model.player.Player;
import ir.fum.siliconvalley.pattern.command.CommandManager;
import ir.fum.siliconvalley.pattern.memento.GameSnapshotCodec;
import ir.fum.siliconvalley.persistence.JavaSerializationGameSaveService;
import ir.fum.siliconvalley.util.GameConstants;
import ir.fum.siliconvalley.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/** JavaFX entry point. */
public final class App extends Application {
    @Override
    public void start(Stage stage) {
        GameController controller = createDemoController();
        MainView mainView = new MainView(controller);
        Scene scene = new Scene(mainView.getRoot(), 1120, 820);
        Image gameIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("/GameIcon.png")));
        var stylesheet = App.class.getResource("/styles/app.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
        stage.setTitle("Silicon Valley: The Tech Cartel");
        stage.getIcons().add(gameIcon);
        stage.setScene(scene);
        stage.show();
    }

    private static GameController createDemoController() {
        Board board = new RandomBoardFactory().create(GameConstants.DEFAULT_BOARD_SIZE, new Random(42));
        Player first = new Player("Ada", PlayerColor.BLUE);
        Player second = new Player("Linus", PlayerColor.RED);
        Game game = new Game(board, new Market(), List.of(first, second));
        StandardGameEngine engine = new StandardGameEngine(game, new Random(1));
        CommandManager commandManager = new CommandManager(engine, new GameSnapshotCodec());
        return new GameController(engine, commandManager, new JavaSerializationGameSaveService());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
