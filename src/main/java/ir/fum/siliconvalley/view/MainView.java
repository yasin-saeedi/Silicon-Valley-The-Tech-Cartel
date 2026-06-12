package ir.fum.siliconvalley.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** Minimal JavaFX shell. Full board rendering is intentionally deferred to the UI phase. */
public final class MainView {
    private final BorderPane root = new BorderPane();

    public MainView() {
        Label title = new Label("Silicon Valley: The Tech Cartel");
        title.getStyleClass().add("title-label");

        Label subtitle = new Label(
                "Phase 1 architecture baseline is ready. " +
                "The next phase connects gameplay rules and the graphical board."
        );
        subtitle.setWrapText(true);
        subtitle.getStyleClass().add("subtitle-label");

        VBox card = new VBox(14, title, subtitle);
        card.getStyleClass().add("phase-card");
        card.setMaxWidth(760);

        VBox center = new VBox(card);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(32));
        root.setCenter(center);
    }

    public Parent getRoot() {
        return root;
    }
}
