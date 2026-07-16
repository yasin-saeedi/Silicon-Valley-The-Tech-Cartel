package ir.fum.siliconvalley.view.setup;

import ir.fum.siliconvalley.model.enums.FounderRole;
import ir.fum.siliconvalley.model.enums.PlayerColor;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Initial setup shown before the game starts.
 * 1- choose number of players (2-4).
 * 2- each player enters a name and picks a founder role.
 */
public final class SetupView extends VBox {

    private static final List<PlayerColor> COLOR_ORDER =
            List.of(PlayerColor.BLUE, PlayerColor.RED, PlayerColor.GREEN, PlayerColor.YELLOW);

    private static final StringConverter<FounderRole> ROLE_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(FounderRole role) {
            return role == null ? "None" : role.toString();
        }

        @Override
        public FounderRole fromString(String string) {
            return null; // combo box is not editable, so this is never invoked
        }
    };

    private final Consumer<List<PlayerSetupData>> onComplete;
    private final VBox pageContainer = new VBox();

    private int playerCount = 2;
    private final List<TextField> nameFields = new ArrayList<>();
    private final List<ComboBox<FounderRole>> roleBoxes = new ArrayList<>();

    public SetupView(Consumer<List<PlayerSetupData>> onComplete) {
        this.onComplete = onComplete;
        getStyleClass().add("setup-view");
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPadding(new Insets(32));

        Label title = new Label("SILICON VALLEY");
        title.getStyleClass().add("board-title");
        Label subtitle = new Label("GAME SETUP");
        subtitle.getStyleClass().add("board-subtitle");

        pageContainer.setAlignment(Pos.CENTER);
        pageContainer.setSpacing(16);

        getChildren().addAll(title, subtitle, pageContainer);
        showPlayerCountPage();
    }

    private void showPlayerCountPage() {
        pageContainer.getChildren().clear();

        Label firstQ = new Label("How many players?");
        firstQ.getStyleClass().add("setup-prompt");

        ComboBox<Integer> countBox = new ComboBox<>();
        countBox.getItems().addAll(2, 3, 4);
        countBox.setValue(playerCount);
        countBox.setOnAction(e -> playerCount = countBox.getValue());

        Button next = new Button("Continue");
        next.setOnAction(e -> showPlayerFormsPage());

        pageContainer.getChildren().addAll(firstQ, countBox, next);
    }

    private void showPlayerFormsPage() {
        pageContainer.getChildren().clear();
        nameFields.clear();
        roleBoxes.clear();

        FounderRole[] roles = FounderRole.values();

        for (int i = 0; i < playerCount; i++) {
            PlayerColor color = COLOR_ORDER.get(i);

            Circle swatch = new Circle(8);
            swatch.setFill(toFxColor(color));

            TextField nameField = new TextField();
            nameField.setPromptText("Player " + (i + 1) + " name");
            nameFields.add(nameField);

            ComboBox<FounderRole> roleBox = new ComboBox<>();
            roleBox.setConverter(ROLE_CONVERTER);
            roleBox.getItems().addAll(roles);
            roleBox.getItems().add(null); // "None" - list is set once, never mutated again
            roleBoxes.add(roleBox);

            HBox row = new HBox(12, swatch, nameField, roleBox);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("setup-player-row");
            pageContainer.getChildren().add(row);
        }

        for (int i = 0; i < roleBoxes.size(); i++) {
            FounderRole defaultRole = i < roles.length ? roles[i] : null;
            roleBoxes.get(i).setValue(defaultRole);
        }

        // taking a role someone else holds bumps that player to None.
        for (ComboBox<FounderRole> box : roleBoxes) {
            box.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    return;
                }
                for (ComboBox<FounderRole> other : roleBoxes) {
                    if (other != box && other.getValue() == newVal) {
                        other.setValue(null);
                    }
                }
            });
        }

        Button back = new Button("Back");
        back.setOnAction(e -> showPlayerCountPage());

        Button start = new Button("Start Game");
        start.setOnAction(e -> tryComplete());

        HBox actions = new HBox(12, back, start);
        actions.setAlignment(Pos.CENTER);
        pageContainer.getChildren().add(actions);
    }

    private void tryComplete() {
        List<PlayerSetupData> players = new ArrayList<>();
        for (int i = 0; i < playerCount; i++) {
            String name = nameFields.get(i).getText();
            if (name == null || name.isBlank()) {
                showValidationError("Please enter a name for Player " + (i + 1) + ".");
                return;
            }
            FounderRole role = roleBoxes.get(i).getValue();
            players.add(new PlayerSetupData(name.trim(), COLOR_ORDER.get(i), role));
        }
        onComplete.accept(players);
    }

    private static void showValidationError(String message) {
        Alert alert = new Alert(AlertType.WARNING, message);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private static javafx.scene.paint.Color toFxColor(PlayerColor color) {
        return switch (color) {
            case BLUE -> javafx.scene.paint.Color.web("#38bdf8");
            case RED -> javafx.scene.paint.Color.web("#f87171");
            case GREEN -> javafx.scene.paint.Color.web("#4ade80");
            case YELLOW -> javafx.scene.paint.Color.web("#facc15");
        };
    }
}
