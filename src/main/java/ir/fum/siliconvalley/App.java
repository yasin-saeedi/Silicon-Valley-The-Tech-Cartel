package ir.fum.siliconvalley;

import ir.fum.siliconvalley.controller.GameController;
import ir.fum.siliconvalley.model.board.Board;
import ir.fum.siliconvalley.model.board.RandomBoardFactory;
import ir.fum.siliconvalley.model.game.Game;
import ir.fum.siliconvalley.model.game.StandardGameEngine;
import ir.fum.siliconvalley.model.market.Market;
import ir.fum.siliconvalley.model.player.Player;
import ir.fum.siliconvalley.pattern.command.CommandManager;
import ir.fum.siliconvalley.pattern.memento.GameSnapshotCodec;
import ir.fum.siliconvalley.persistence.JavaSerializationGameSaveService;
import ir.fum.siliconvalley.util.GameConstants;
import ir.fum.siliconvalley.view.MainView;
import ir.fum.siliconvalley.view.setup.BoardSizeSetupView;
import ir.fum.siliconvalley.view.setup.PlayerSetupData;
import ir.fum.siliconvalley.view.setup.SetupView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** JavaFX entry point. */
public final class App extends Application {

    @Override
    public void start(Stage stage) {
        Image gameIcon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("/GameIcon.png")));
        stage.setTitle("Silicon Valley: The Tech Cartel");
        stage.getIcons().add(gameIcon);

        showSetupScene(stage);
        stage.show();
    }

    /** First screen - board size selection setup */
    private void showSetupScene(Stage stage) {
        BoardSizeSetupView sizeView = new BoardSizeSetupView(boardSize -> showPlayerSetupScene(stage, boardSize));
        Scene scene = new Scene(sizeView, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        applyStylesheet(scene);
        stage.setScene(scene);
    }

    /** Second screen - player count + name/role setup */
    private void showPlayerSetupScene(Stage stage, int boardSize) {
        SetupView setupView = new SetupView(players -> startGame(stage,players,boardSize));
        Scene scene = new Scene(setupView, GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        applyStylesheet(scene);
        stage.setScene(scene);
    }

    /** Called once the setups collect all players then swaps in the real game scene */
    private void startGame(Stage stage, List<PlayerSetupData> playerSetups ,int boardSize) {
        GameController controller = createController(playerSetups ,boardSize);
        MainView mainView = new MainView(controller);
        Scene scene = new Scene(mainView.getRoot(), GameConstants.WINDOW_WIDTH, GameConstants.WINDOW_HEIGHT);
        applyStylesheet(scene);
        stage.setScene(scene);
    }

    private static void applyStylesheet(Scene scene) {
        var stylesheet = App.class.getResource("/styles/app.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
    }

    private static GameController createController(List<PlayerSetupData> playerSetupDataList ,int boardSize) {
        Board board = new RandomBoardFactory().create(boardSize, new Random());

        List<Player> players = new ArrayList<>();
        for (PlayerSetupData setup : playerSetupDataList) {
            Player player = new Player(setup.name(), setup.color());
            if (setup.role() != null) {
                try {
                    player.assignFounderRole(setup.role());
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to assign founder role for " + setup.name(), e);
                }
            }
            players.add(player);
        }

        Game game = new Game(board, new Market(), players);
        StandardGameEngine engine = new StandardGameEngine(game, new Random());
        CommandManager commandManager = new CommandManager(engine, new GameSnapshotCodec());
        return new GameController(engine, commandManager, new JavaSerializationGameSaveService());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
