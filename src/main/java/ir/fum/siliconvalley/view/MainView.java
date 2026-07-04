package ir.fum.siliconvalley.view;

import ir.fum.siliconvalley.controller.GameController;
import ir.fum.siliconvalley.view.board.BoardView;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/** Top-level layout shell. Positions gameplay panels without owning game rules. */
public final class MainView {
    private final BorderPane root = new BorderPane();
    private final BoardView boardView;

    public MainView(GameController controller) {
        boardView = new BoardView(controller);
        root.setCenter(boardView);
    }

    public BoardView getBoardView() {
        return boardView;
    }

    public Parent getRoot() {
        return root;
    }
}
