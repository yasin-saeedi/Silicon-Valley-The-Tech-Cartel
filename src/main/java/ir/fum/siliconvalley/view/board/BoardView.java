package ir.fum.siliconvalley.view.board;

import ir.fum.siliconvalley.controller.GameController;
import ir.fum.siliconvalley.model.board.Board;
import ir.fum.siliconvalley.model.board.Edge;
import ir.fum.siliconvalley.model.board.EdgePosition;
import ir.fum.siliconvalley.model.board.Sector;
import ir.fum.siliconvalley.model.board.SectorPosition;
import ir.fum.siliconvalley.model.board.Vertex;
import ir.fum.siliconvalley.model.board.VertexPosition;
import ir.fum.siliconvalley.model.enums.PlayerColor;
import ir.fum.siliconvalley.model.enums.SectorType;
import ir.fum.siliconvalley.model.game.Game;
import ir.fum.siliconvalley.model.player.Player;
import ir.fum.siliconvalley.model.structure.CompanyStructure;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import ir.fum.siliconvalley.util.GameConstants;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Renders the game board: sectors, edges, and vertices. */
public final class BoardView extends VBox {

    private final GameController controller;
    private final GridPane sectorGrid = new GridPane();
    private final Pane overlay = new Pane();
    private final StackPane boardCanvas = new StackPane();

    public BoardView(GameController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
        getStyleClass().add("board-view");
        setAlignment(Pos.TOP_CENTER);
        setSpacing(14);
        setPadding(new Insets(16, 20, 16, 20));

        Label title = new Label("SILICON VALLEY");
        title.getStyleClass().add("board-title");
        Label subtitle = new Label("THE TECH CARTEL");
        subtitle.getStyleClass().add("board-subtitle");

        boardCanvas.getStyleClass().add("board-canvas");
        sectorGrid.setHgap(0);
        sectorGrid.setVgap(0);
        boardCanvas.getChildren().addAll(sectorGrid, overlay);
        resizeBoardCanvas();

        getChildren().addAll(title, subtitle, boardCanvas, createLegend());

        render();
    }

    public void render() {
        sectorGrid.getChildren().clear();
        overlay.getChildren().clear();

        // getting board data via controller
        Board board = controller.getGame().getBoard();
        resizeBoardCanvas();
        int size = board.getSize();

        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                Sector sector = board.getSector(new SectorPosition(row, column));
                sectorGrid.add(createSectorTile(sector), column, row);
            }
        }

        for (Edge edge : board.getEdges()) {
            overlay.getChildren().add(createEdgeNode(edge));
        }
        for (Vertex vertex : board.getVertices()) {
            overlay.getChildren().add(createVertexNode(vertex));
        }
    }

    private VBox createSectorTile(Sector sector) {
        SectorType type = sector.getType();
        int row = sector.getPosition().row();

        Label nameLabel = new Label(SectorDisplay.sectorName(type, row));
        nameLabel.getStyleClass().add("sector-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(GameConstants.CELL_WIDTH - 16);

        Label numberLabel = new Label(formatActivationNumber(sector));
        numberLabel.getStyleClass().add("sector-number");

        Pane icon = SectorDisplay.icon(type);

        VBox tile = new VBox(2, nameLabel, numberLabel, icon);
        tile.getStyleClass().addAll("sector-tile", SectorDisplay.styleClass(type));
        tile.setAlignment(Pos.CENTER);
        tile.setPrefSize(GameConstants.CELL_WIDTH, GameConstants.CELL_HEIGHT);
        tile.setMinSize(GameConstants.CELL_WIDTH, GameConstants.CELL_HEIGHT);
        tile.setMaxSize(GameConstants.CELL_WIDTH, GameConstants.CELL_HEIGHT);

        // Audited Sector
        if (sector.isAudited()) {
            tile.getStyleClass().add("sector-audited");
        }
        controller.getGame().getBoard().getAuditorPosition()
                .filter(position -> position.equals(sector.getPosition()))
                .ifPresent(ignored -> tile.getStyleClass().add("sector-auditor-here"));

        return tile;
    }

    private Rectangle createEdgeNode(Edge edge) {
        EdgePosition position = edge.getPosition();
        VertexPosition first = position.first();
        VertexPosition second = position.second();

        boolean horizontal = first.row() == second.row();
        double x;
        double y;
        double width;
        double height;

        if (horizontal) {
            int row = first.row();
            int column = Math.min(first.column(), second.column());
            x = column * GameConstants.CELL_WIDTH;
            y = row * GameConstants.CELL_HEIGHT - GameConstants.EDGE_THICKNESS / 2.0;
            width = GameConstants.CELL_WIDTH;
            height = GameConstants.EDGE_THICKNESS;
        }
        else {
            int row = Math.min(first.row(), second.row());
            int column = first.column();
            x = column * GameConstants.CELL_WIDTH - GameConstants.EDGE_THICKNESS / 2.0;
            y = row * GameConstants.CELL_HEIGHT;
            width = GameConstants.EDGE_THICKNESS;
            height = GameConstants.CELL_HEIGHT;
        }

        Rectangle node = new Rectangle(width, height);
        node.getStyleClass().add("board-edge");
        node.setLayoutX(x);
        node.setLayoutY(y);

        resolveStructureOwner(edge.getPartnershipId()).ifPresent(player -> {
            node.getStyleClass().add("board-edge-occupied");
            node.setFill(toFxColor(player.getColor()));
        });

        return node;
    }

    private Circle createVertexNode(Vertex vertex) {
        VertexPosition position = vertex.getPosition();
        Circle node = new Circle(GameConstants.VERTEX_RADIUS);
        node.getStyleClass().add("board-vertex");
        node.setLayoutX(position.column() * GameConstants.CELL_WIDTH);
        node.setLayoutY(position.row() * GameConstants.CELL_HEIGHT);

        resolveStructureOwner(vertex.getCompanyStructureId()).ifPresent(player -> {
            node.getStyleClass().add("board-vertex-occupied");
            node.setFill(toFxColor(player.getColor()));
        });

        // build company by click
        node.setOnMouseClicked(e -> {
            /*controller.bulidMVP(vertex.getPosition());
            render();*/
        });

        node.setCursor(javafx.scene.Cursor.HAND);
        return node;
    }

    private Optional<Player> resolveStructureOwner(Optional<UUID> structureId) {
        if (structureId.isEmpty()) {
            return Optional.empty();
        }
        Game game = controller.getGame();
        Optional<CompanyStructure> structure = game.findStructure(structureId.get());
        return structure.flatMap(item -> game.findPlayer(item.getOwnerId()));
    }

    private static String formatActivationNumber(Sector sector) {
        if (sector.getType() == SectorType.REGULATORY_ZONE) {
            return "";
        }
        return sector.getActivationNumber()
                .stream()
                .mapToObj(Integer::toString)
                .findFirst()
                .orElse("");
    }

    private void resizeBoardCanvas() {
        int size = controller.getGame().getBoard().getSize();
        double width = GameConstants.CELL_WIDTH * size;
        double height = GameConstants.CELL_HEIGHT * size;
        boardCanvas.setMinSize(width, height);
        boardCanvas.setPrefSize(width, height);
        boardCanvas.setMaxSize(width, height);
    }

    private VBox createLegend() {
        VBox legend = new VBox(8);
        legend.getStyleClass().add("board-legend");
        legend.setAlignment(Pos.CENTER);

        GridPane legendGrid = new GridPane();
        legendGrid.getStyleClass().add("board-legend-grid");
        legendGrid.setHgap(18);
        legendGrid.setVgap(6);
        legendGrid.setAlignment(Pos.CENTER);

        for (int i = 0; i < 6; i++) {  // SectorTypes
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHalignment(HPos.CENTER);
            cc.setPrefWidth(120);
            cc.setMinWidth(20);
            cc.setHgrow(Priority.ALWAYS);
            legendGrid.getColumnConstraints().add(cc);
        }

        SectorType[] types = {
                SectorType.AI_HUB,
                SectorType.FINTECH_DISTRICT,
                SectorType.CLOUD_CAMPUS,
                SectorType.DATA_VALLEY,
                SectorType.IP_QUARTER,
                SectorType.REGULATORY_ZONE
        };
        for (int index = 0; index < types.length; index++) {
            SectorType type = types[index];
            
            StackPane swatch = new StackPane();
            swatch.getChildren().add(SectorDisplay.icon(type));
            swatch.getStyleClass().addAll("legend-swatch", SectorDisplay.styleClass(type));

            Label label = new Label(SectorDisplay.categoryLabel(type));
            label.getStyleClass().add("legend-label");

            legendGrid.add(swatch, index, 0);
            legendGrid.add(label, index, 1);
        }

        legend.getChildren().add(legendGrid);
        return legend;
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
