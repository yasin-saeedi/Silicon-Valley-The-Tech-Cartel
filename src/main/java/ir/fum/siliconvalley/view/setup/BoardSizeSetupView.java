package ir.fum.siliconvalley.view.setup;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.function.IntConsumer;

/** First screen - choose the board size before any player setup happens */
public final class BoardSizeSetupView extends VBox {

    public BoardSizeSetupView(IntConsumer onSizeChosen) {
        getStyleClass().add("setup-view");
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(40));

        Label title = new Label("Select Board Size");
        title.getStyleClass().add("setup-prompt");

        ComboBox<Integer> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(5, 6, 7);
        sizeBox.setValue(6);

        Button continueButton = new Button("Continue");
        continueButton.setOnAction(e -> onSizeChosen.accept(sizeBox.getValue()));

        getChildren().addAll(title, sizeBox, continueButton);
    }
}