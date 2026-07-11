package ir.fum.siliconvalley.view;

import ir.fum.siliconvalley.controller.GameController;
import ir.fum.siliconvalley.model.board.EdgePosition;
import ir.fum.siliconvalley.model.board.SectorPosition;
import ir.fum.siliconvalley.model.board.VertexPosition;
import ir.fum.siliconvalley.model.enums.GamePhase;
import ir.fum.siliconvalley.model.enums.ResourceType;
import ir.fum.siliconvalley.model.enums.TurnStage;
import ir.fum.siliconvalley.model.game.Game;
import ir.fum.siliconvalley.model.market.Market;
import ir.fum.siliconvalley.model.player.Player;
import ir.fum.siliconvalley.model.resource.ResourceBundle;
import ir.fum.siliconvalley.pattern.command.*;
import ir.fum.siliconvalley.util.GameConstants;
import ir.fum.siliconvalley.view.board.BoardView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/** Top-level layout shell. Positions gameplay panels and actions dashboard. */
public final class MainView {
    private final BorderPane root = new BorderPane();
    private final BoardView boardView;
    private final GameController controller;

    // Sidebar UI elements
    private final VBox playerListContainer = new VBox(10);
    private final Label stageLabel = new Label();
    private final Label roundLabel = new Label();
    private final Button rollDiceBtn = new Button("Roll Dice");
    private final Button endTurnBtn = new Button("End Turn");
    private final Button tradeBtn = new Button("Trade with Player");
    private boolean victoryOverlayShown = false;
    
    // Construction Toggles
    private final ToggleButton buildMvpToggle = new ToggleButton("Build MVP");
    private final ToggleButton buildPartnershipToggle = new ToggleButton("Build Partnership");
    private final ToggleButton upgradeUnicornToggle = new ToggleButton("Upgrade Unicorn");
    private final ToggleGroup actionToggles = new ToggleGroup();

    // Market Buy buttons and labels
    private final VBox marketContainer = new VBox(8);

    // Logging
    private final TextArea logArea = new TextArea();

    // Setup Phase interaction helper state
    private VertexPosition selectedSetupVertex = null;

    public MainView(GameController controller) {
        this.controller = Objects.requireNonNull(controller, "controller");
        this.boardView = new BoardView(controller);
        this.boardView.setMainView(this);

        root.setCenter(boardView);
        root.setRight(createSidebar());

        // Initial update
        refresh();
    }

    public BoardView getBoardView() {
        return boardView;
    }

    public Parent getRoot() {
        return root;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(14);
        sidebar.setPadding(new Insets(16));
        sidebar.setPrefWidth(340);
        sidebar.getStyleClass().add("dashboard-panel");
        sidebar.setStyle("-fx-background-color: #0b0f19; -fx-border-color: #1e293b; -fx-border-width: 0 0 0 1;");

        // Game Status Header
        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Control Dashboard");
        title.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: bold; -fx-font-size: 16px;");
        roundLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
        stageLabel.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold; -fx-font-size: 13px;");
        header.getChildren().addAll(title, roundLabel, stageLabel);

        // Player section
        Label playersHeader = new Label("Players Inventory");
        playersHeader.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 12px;");
        VBox playerSection = new VBox(6, playersHeader, playerListContainer);

        // Core Actions Section
        VBox actionsBox = new VBox(8);
        rollDiceBtn.setMaxWidth(Double.MAX_VALUE);
        rollDiceBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        rollDiceBtn.setOnAction(e -> executeCommand(new RollDiceCommand()));

        endTurnBtn.setMaxWidth(Double.MAX_VALUE);
        endTurnBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        endTurnBtn.setOnAction(e -> executeCommand(new EndTurnCommand()));

        HBox togglesBox = new HBox(6);
        buildMvpToggle.setToggleGroup(actionToggles);
        buildPartnershipToggle.setToggleGroup(actionToggles);
        upgradeUnicornToggle.setToggleGroup(actionToggles);

        String toggleStyle = "-fx-background-color: #1e293b; -fx-text-fill: #cbd5e1; -fx-font-size: 11px; -fx-padding: 6;";
        buildMvpToggle.setStyle(toggleStyle);
        buildPartnershipToggle.setStyle(toggleStyle);
        upgradeUnicornToggle.setStyle(toggleStyle);
        
        buildMvpToggle.setMaxWidth(Double.MAX_VALUE);
        buildPartnershipToggle.setMaxWidth(Double.MAX_VALUE);
        upgradeUnicornToggle.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(buildMvpToggle, Priority.ALWAYS);
        HBox.setHgrow(buildPartnershipToggle, Priority.ALWAYS);
        HBox.setHgrow(upgradeUnicornToggle, Priority.ALWAYS);
        
        togglesBox.getChildren().addAll(buildMvpToggle, buildPartnershipToggle, upgradeUnicornToggle);

        tradeBtn.setMaxWidth(Double.MAX_VALUE);
        tradeBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8;");
        tradeBtn.setOnAction(e -> showTradeDialog());

        actionsBox.getChildren().addAll(rollDiceBtn, togglesBox, tradeBtn, endTurnBtn);

        // Persistence and Undo Section
        HBox utilityBox = new HBox(6);
        Button undoBtn = new Button("Undo");
        Button redoBtn = new Button("Redo");
        Button saveBtn = new Button("Save");
        Button loadBtn = new Button("Load");

        String utilStyle = "-fx-background-color: #334155; -fx-text-fill: #cbd5e1; -fx-font-size: 11px; -fx-padding: 6;";
        undoBtn.setStyle(utilStyle);
        redoBtn.setStyle(utilStyle);
        saveBtn.setStyle(utilStyle);
        loadBtn.setStyle(utilStyle);
        
        undoBtn.setMaxWidth(Double.MAX_VALUE);
        redoBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        loadBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(undoBtn, Priority.ALWAYS);
        HBox.setHgrow(redoBtn, Priority.ALWAYS);
        HBox.setHgrow(saveBtn, Priority.ALWAYS);
        HBox.setHgrow(loadBtn, Priority.ALWAYS);

        undoBtn.setOnAction(e -> controller.undo(this::refresh));
        redoBtn.setOnAction(e -> controller.redo(this::refresh));
        saveBtn.setOnAction(e -> handleSave());
        loadBtn.setOnAction(e -> handleLoad());

        utilityBox.getChildren().addAll(undoBtn, redoBtn, saveBtn, loadBtn);

        // Market Container
        Label marketHeader = new Label("Dynamic Tech Market");
        marketHeader.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 12px;");
        VBox marketSection = new VBox(6, marketHeader, marketContainer);

        // Log Console
        logArea.setEditable(false);
        logArea.setPrefHeight(100);
        logArea.setWrapText(true);
        logArea.setStyle("-fx-control-inner-background: #020617; -fx-text-fill: #38bdf8; -fx-font-family: monospace; -fx-font-size: 10px;");

        sidebar.getChildren().addAll(header, playerSection, actionsBox, utilityBox, marketSection, logArea);
        return sidebar;
    }

    public void refresh() {
        Game game = controller.getGame();
        boardView.render();

        // Check Victory
        checkVictory();

        // 1. Header info
        roundLabel.setText("Full Round: " + game.getFullRoundNumber());
        stageLabel.setText("Phase: " + game.getPhase() + " | Stage: " + game.getTurnStage());

        // 2. Refresh Player List
        playerListContainer.getChildren().clear();
        Player activePlayer = game.getCurrentPlayer();
        UUID longestOwner = game.getLongestNetworkOwnerId().orElse(null);

        for (Player p : game.getPlayers()) {
            VBox card = new VBox(4);
            card.setPadding(new Insets(8));
            
            // Calculate total victory points
            int baseVp = p.calculateBaseVictoryPoints();
            boolean isLongestOwner = p.getId().equals(longestOwner);
            int totalVp = baseVp + (isLongestOwner ? GameConstants.LONGEST_NETWORK_BONUS : 0);

            String borderCol = p.getId().equals(activePlayer.getId()) ? "#3b82f6" : "#1e293b";
            String bgCol = p.getId().equals(activePlayer.getId()) ? "rgba(59, 130, 246, 0.15)" : "#0f172a";
            card.setStyle(String.format("-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;", bgCol, borderCol));

            HBox titleRow = new HBox(6);
            titleRow.setAlignment(Pos.CENTER_LEFT);
            Pane dot = new Pane();
            dot.setMinSize(10, 10);
            dot.setMaxSize(10, 10);
            dot.setStyle("-fx-background-color: " + toHexColor(p.getColor()) + "; -fx-background-radius: 5;");
            
            Label nameLbl = new Label(p.getName() + (p.getFounderRole().map(r -> " (" + r.name() + ")").orElse("")));
            nameLbl.setStyle("-fx-text-fill: #f1f5f9; -fx-font-weight: bold; -fx-font-size: 12px;");
            
            Label vpLbl = new Label("VP: " + totalVp + " (" + baseVp + " base" + (isLongestOwner ? " + 2 network" : "") + ")");
            vpLbl.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold; -fx-font-size: 11px;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            titleRow.getChildren().addAll(dot, nameLbl, spacer, vpLbl);

            // Resources details
            HBox resourcesRow = new HBox(8);
            resourcesRow.setAlignment(Pos.CENTER_LEFT);
            for (ResourceType resType : ResourceType.values()) {
                Label rLbl = new Label(resType.name().substring(0, 3) + ":" + p.getResourceCount(resType));
                rLbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
                resourcesRow.getChildren().add(rLbl);
            }

            card.getChildren().addAll(titleRow, resourcesRow);
            playerListContainer.getChildren().add(card);
        }

        // 3. Action button toggling
        TurnStage stage = game.getTurnStage();
        rollDiceBtn.setDisable(stage != TurnStage.ROLL_DICE || game.getPhase() == GamePhase.SETUP);
        endTurnBtn.setDisable(stage != TurnStage.ACTIONS);
        
        boolean actionsAllowed = stage == TurnStage.ACTIONS && game.getPhase() == GamePhase.MAIN_TURN;
        buildMvpToggle.setDisable(!actionsAllowed);
        buildPartnershipToggle.setDisable(!actionsAllowed);
        upgradeUnicornToggle.setDisable(!actionsAllowed);
        tradeBtn.setDisable(!actionsAllowed);

        if (!actionsAllowed) {
            actionToggles.selectToggle(null);
        }

        // 4. Refresh Market buy panel
        marketContainer.getChildren().clear();
        Market market = game.getMarket();
        for (ResourceType type : ResourceType.values()) {
            HBox mRow = new HBox(8);
            mRow.setAlignment(Pos.CENTER_LEFT);
            mRow.setPadding(new Insets(4, 6, 4, 6));
            mRow.setStyle("-fx-background-color: #0f172a; -fx-background-radius: 4; -fx-border-color: #1e293b; -fx-border-width: 1;");

            Label nameLabel = new Label(type.name());
            nameLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-weight: bold; -fx-font-size: 11px;");
            nameLabel.setPrefWidth(70);

            int effectivePrice = market.getEffectivePrice(type, activePlayer.getFounderRole());
            Label priceLabel = new Label("Price: " + effectivePrice + " Cap");
            priceLabel.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 11px;");
            priceLabel.setPrefWidth(90);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button buyBtn = new Button("Buy");
            buyBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 3 8 3 8; -fx-font-weight: bold;");
            buyBtn.setDisable(!actionsAllowed || activePlayer.getResourceCount(ResourceType.CAPITAL) < effectivePrice);
            buyBtn.setOnAction(e -> executeCommand(new BuyFromMarketCommand(activePlayer.getId(), type)));

            mRow.getChildren().addAll(nameLabel, priceLabel, spacer, buyBtn);
            marketContainer.getChildren().add(mRow);
        }

        // 5. Trigger Tax Discard Dialog if active
        if (game.getPhase() == GamePhase.MAIN_TURN && stage == TurnStage.TAX_DISCARD) {
            for (Player player : game.getPlayers()) {
                int limit = player.getTaxHandLimit();
                int totalCards = player.getTotalResourceCards();
                if (totalCards > limit) {
                    int required = totalCards / 2;
                    showTaxDiscardDialog(player, required);
                    break;
                }
            }
        }
    }

    private void executeCommand(GameCommand command) {
        logEvent("Submitting action: " + command.description());
        controller.execute(command, () -> {
            logEvent("Action successful: " + command.description());
            refresh();
        }, throwable -> {
            logEvent("Action failed: " + throwable.getMessage());
            showError(throwable.getMessage());
        });
    }

    private void handleSave() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Game State");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Silicon Valley Saves (*.sav)", "*.sav"));
        File file = chooser.showSaveDialog(root.getScene().getWindow());
        if (file != null) {
            Path path = file.toPath();
            controller.save(path, () -> logEvent("Game saved successfully to " + file.getName()),
                    throwable -> showError("Save failed: " + throwable.getMessage()));
        }
    }

    private void handleLoad() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Game State");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Silicon Valley Saves (*.sav)", "*.sav"));
        File file = chooser.showOpenDialog(root.getScene().getWindow());
        if (file != null) {
            Path path = file.toPath();
            controller.load(path, () -> {
                logEvent("Game loaded successfully from " + file.getName());
                refresh();
            }, throwable -> showError("Load failed: " + throwable.getMessage()));
        }
    }

    public void handleVertexClick(VertexPosition position) {
        UUID activePlayerId = controller.getGame().getCurrentPlayer().getId();
        Game game = controller.getGame();

        if (game.getPhase() == GamePhase.SETUP) {
            selectedSetupVertex = position;
            logEvent("Selected vertex " + position + " for initial setup. Now click a connected edge.");
            return;
        }

        if (buildMvpToggle.isSelected()) {
            executeCommand(new BuildMvpCommand(activePlayerId, position));
            buildMvpToggle.setSelected(false);
        } else if (upgradeUnicornToggle.isSelected()) {
            Optional<UUID> structureId = game.getBoard().getVertex(position).getCompanyStructureId();
            if (structureId.isPresent()) {
                executeCommand(new UpgradeUnicornCommand(activePlayerId, structureId.get()));
            } else {
                showError("No company structure at this vertex to upgrade.");
            }
            upgradeUnicornToggle.setSelected(false);
        }
    }

    public void handleEdgeClick(EdgePosition position) {
        UUID activePlayerId = controller.getGame().getCurrentPlayer().getId();
        Game game = controller.getGame();

        if (game.getPhase() == GamePhase.SETUP) {
            if (selectedSetupVertex == null) {
                showError("Please select a vertex first for setup placement.");
                return;
            }
            executeCommand(new PlaceSetupCommand(activePlayerId, selectedSetupVertex, position));
            selectedSetupVertex = null;
            return;
        }

        if (buildPartnershipToggle.isSelected()) {
            executeCommand(new BuildPartnershipCommand(activePlayerId, position));
            buildPartnershipToggle.setSelected(false);
        }
    }

    public void handleSectorClick(SectorPosition position) {
        Game game = controller.getGame();
        if (game.getPhase() == GamePhase.MAIN_TURN && game.getTurnStage() == TurnStage.AUDITOR_MOVE) {
            UUID activePlayerId = game.getCurrentPlayer().getId();
            executeCommand(new MoveAuditorCommand(activePlayerId, position));
        }
    }

    private void showTaxDiscardDialog(Player player, int requiredCount) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(root.getScene().getWindow());
        dialog.setTitle("Tax Discard - " + player.getName());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(16));
        vbox.setStyle("-fx-background-color: #0b0f19; -fx-border-color: #1e293b; -fx-border-width: 1;");

        Label info = new Label(player.getName() + ", you have " + player.getTotalResourceCards() + 
                " cards. You must discard exactly " + requiredCount + " cards.");
        info.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: bold; -fx-font-size: 13px;");
        vbox.getChildren().add(info);

        Map<ResourceType, Integer> chosen = new HashMap<>();
        for (ResourceType type : ResourceType.values()) {
            int owned = player.getResourceCount(type);
            if (owned > 0) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                Label label = new Label(type.name() + " (Owned: " + owned + ")");
                label.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");
                label.setPrefWidth(120);

                Label countLabel = new Label("0");
                countLabel.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold; -fx-font-size: 13px;");
                countLabel.setPrefWidth(20);
                countLabel.setAlignment(Pos.CENTER);

                Button dec = new Button("-");
                dec.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-weight: bold;");
                dec.setOnAction(e -> {
                    int val = chosen.getOrDefault(type, 0);
                    if (val > 0) {
                        chosen.put(type, val - 1);
                        countLabel.setText(String.valueOf(val - 1));
                    }
                });

                Button inc = new Button("+");
                inc.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-weight: bold;");
                inc.setOnAction(e -> {
                    int val = chosen.getOrDefault(type, 0);
                    if (val < owned) {
                        chosen.put(type, val + 1);
                        countLabel.setText(String.valueOf(val + 1));
                    }
                });

                row.getChildren().addAll(label, dec, countLabel, inc);
                vbox.getChildren().add(row);
            }
        }

        Button confirmBtn = new Button("Discard");
        confirmBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 16 6 16;");
        confirmBtn.setOnAction(e -> {
            int totalChosen = chosen.values().stream().mapToInt(Integer::intValue).sum();
            if (totalChosen != requiredCount) {
                showError("You must choose exactly " + requiredCount + " cards. Current selected: " + totalChosen);
                return;
            }
            ResourceBundle.Builder builder = ResourceBundle.builder();
            chosen.forEach((type, count) -> {
                if (count > 0) {
                    builder.add(type, count);
                }
            });
            ResourceBundle bundle = builder.build();
            controller.execute(new DiscardTaxCommand(player.getId(), bundle), () -> {
                dialog.close();
                logEvent("Player " + player.getName() + " discarded " + requiredCount + " tax cards.");
                refresh();
            }, throwable -> showError("Discard failed: " + throwable.getMessage()));
        });

        vbox.getChildren().add(confirmBtn);
        Scene scene = new Scene(vbox);
        dialog.setScene(scene);
        dialog.show();
    }

    private void showTradeDialog() {
        Game game = controller.getGame();
        Player activePlayer = game.getCurrentPlayer();
        List<Player> otherPlayers = game.getPlayers().stream()
                .filter(p -> !p.getId().equals(activePlayer.getId()))
                .toList();

        if (otherPlayers.isEmpty()) {
            showError("No other players available to trade with.");
            return;
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(root.getScene().getWindow());
        dialog.setTitle("Player-to-Player Trade - " + activePlayer.getName());

        VBox vbox = new VBox(12);
        vbox.setPadding(new Insets(16));
        vbox.setStyle("-fx-background-color: #0b0f19; -fx-border-color: #1e293b; -fx-border-width: 1;");

        Label info = new Label("Propose a trade from " + activePlayer.getName() + " to an opponent:");
        info.setStyle("-fx-text-fill: #f8fafc; -fx-font-weight: bold; -fx-font-size: 13px;");

        HBox targetBox = new HBox(8);
        targetBox.setAlignment(Pos.CENTER_LEFT);
        Label targetLbl = new Label("Target Player:");
        targetLbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
        ComboBox<Player> targetCombo = new ComboBox<>();
        targetCombo.getItems().addAll(otherPlayers);
        targetCombo.setValue(otherPlayers.get(0));
        targetCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Player p) {
                return p == null ? "" : p.getName() + " (Cards: " + p.getTotalResourceCards() + ")";
            }
            @Override
            public Player fromString(String string) { return null; }
        });
        targetBox.getChildren().addAll(targetLbl, targetCombo);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER_LEFT);

        Label resHeader = new Label("Resource");
        resHeader.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label offerHeader = new Label("You Offer");
        offerHeader.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold; -fx-font-size: 11px;");
        Label reqHeader = new Label("You Request");
        reqHeader.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");

        grid.add(resHeader, 0, 0);
        grid.add(offerHeader, 1, 0);
        grid.add(reqHeader, 2, 0);

        Map<ResourceType, Integer> offeredMap = new HashMap<>();
        Map<ResourceType, Integer> requestedMap = new HashMap<>();

        int rowIdx = 1;
        for (ResourceType type : ResourceType.values()) {
            int owned = activePlayer.getResourceCount(type);
            Label nameLbl = new Label(type.name() + " (Owned: " + owned + ")");
            nameLbl.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 11px;");

            HBox offerBox = new HBox(6);
            offerBox.setAlignment(Pos.CENTER);
            Label offerVal = new Label("0");
            offerVal.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold;");
            offerVal.setPrefWidth(20);
            offerVal.setAlignment(Pos.CENTER);
            Button offerDec = new Button("-");
            offerDec.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 10px;");
            Button offerInc = new Button("+");
            offerInc.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 10px;");
            offerDec.setOnAction(e -> {
                int val = offeredMap.getOrDefault(type, 0);
                if (val > 0) {
                    offeredMap.put(type, val - 1);
                    offerVal.setText(String.valueOf(val - 1));
                }
            });
            offerInc.setOnAction(e -> {
                int val = offeredMap.getOrDefault(type, 0);
                if (val < owned) {
                    offeredMap.put(type, val + 1);
                    offerVal.setText(String.valueOf(val + 1));
                }
            });
            offerBox.getChildren().addAll(offerDec, offerVal, offerInc);

            HBox reqBox = new HBox(6);
            reqBox.setAlignment(Pos.CENTER);
            Label reqVal = new Label("0");
            reqVal.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
            reqVal.setPrefWidth(20);
            reqVal.setAlignment(Pos.CENTER);
            Button reqDec = new Button("-");
            reqDec.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 10px;");
            Button reqInc = new Button("+");
            reqInc.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-font-size: 10px;");
            reqDec.setOnAction(e -> {
                int val = requestedMap.getOrDefault(type, 0);
                if (val > 0) {
                    requestedMap.put(type, val - 1);
                    reqVal.setText(String.valueOf(val - 1));
                }
            });
            reqInc.setOnAction(e -> {
                int val = requestedMap.getOrDefault(type, 0);
                requestedMap.put(type, val + 1);
                reqVal.setText(String.valueOf(val + 1));
            });
            reqBox.getChildren().addAll(reqDec, reqVal, reqInc);

            grid.add(nameLbl, 0, rowIdx);
            grid.add(offerBox, 1, rowIdx);
            grid.add(reqBox, 2, rowIdx);
            rowIdx++;
        }

        Button proposeBtn = new Button("Propose Trade");
        proposeBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 16 6 16;");
        proposeBtn.setOnAction(e -> {
            Player target = targetCombo.getValue();
            if (target == null) {
                showError("Please select a target player.");
                return;
            }
            ResourceBundle.Builder offBuilder = ResourceBundle.builder();
            offeredMap.forEach((type, count) -> { if (count > 0) offBuilder.add(type, count); });
            ResourceBundle offered = offBuilder.build();

            ResourceBundle.Builder reqBuilder = ResourceBundle.builder();
            requestedMap.forEach((type, count) -> { if (count > 0) reqBuilder.add(type, count); });
            ResourceBundle requested = reqBuilder.build();

            if (offered.totalCards() == 0 && requested.totalCards() == 0) {
                showError("Trade must involve at least one offered or requested resource.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Trade Proposal");
            confirm.setHeaderText(target.getName() + ", do you accept this trade from " + activePlayer.getName() + "?");
            confirm.setContentText("Offered to you: " + offered + "\nRequested from you: " + requested);
            confirm.initOwner(dialog);

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    controller.execute(new TradeWithPlayerCommand(activePlayer.getId(), target.getId(), offered, requested), () -> {
                        dialog.close();
                        logEvent("Trade accepted between " + activePlayer.getName() + " and " + target.getName());
                        refresh();
                    }, throwable -> showError("Trade failed: " + throwable.getMessage()));
                } else {
                    logEvent("Trade proposal declined by " + target.getName());
                }
            });
        });

        vbox.getChildren().addAll(info, targetBox, grid, proposeBtn);
        Scene scene = new Scene(vbox);
        dialog.setScene(scene);
        dialog.show();
    }

    private void checkVictory() {
        if (victoryOverlayShown) {
            return;
        }
        Game game = controller.getGame();
        if (game.getPhase() == GamePhase.FINISHED) {
            victoryOverlayShown = true;
            Player winner = game.getPlayers().stream()
                    .max(Comparator.comparingInt(p -> {
                        int base = p.calculateBaseVictoryPoints();
                        boolean longest = game.getLongestNetworkOwnerId()
                                .map(id -> id.equals(p.getId())).orElse(false);
                        return base + (longest ? GameConstants.LONGEST_NETWORK_BONUS : 0);
                    }))
                    .orElse(game.getCurrentPlayer());
            int totalVp = winner.calculateBaseVictoryPoints() + 
                    (game.getLongestNetworkOwnerId().map(id -> id.equals(winner.getId())).orElse(false) ? GameConstants.LONGEST_NETWORK_BONUS : 0);
            showVictoryOverlay(winner, totalVp);
        }
    }

    private void showVictoryOverlay(Player player, int score) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(root.getScene().getWindow());
        dialog.setTitle("Victory!");

        VBox vbox = new VBox(14);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(24));
        vbox.setStyle("-fx-background-color: #0b0f19; -fx-border-color: #fbbf24; -fx-border-width: 2;");

        Label winLabel = new Label("🏆 VICTORY! 🏆");
        winLabel.setStyle("-fx-text-fill: #fbbf24; -fx-font-weight: bold; -fx-font-size: 24px;");

        Label descLabel = new Label(player.getName() + " reached " + score + " Victory Points and won the game!");
        descLabel.setStyle("-fx-text-fill: #f1f5f9; -fx-font-size: 14px; -fx-font-weight: bold;");

        Button closeBtn = new Button("Close Game");
        closeBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold;");
        closeBtn.setOnAction(e -> {
            dialog.close();
            ((Stage) root.getScene().getWindow()).close();
        });

        vbox.getChildren().addAll(winLabel, descLabel, closeBtn);
        Scene scene = new Scene(vbox);
        dialog.setScene(scene);
        dialog.show();
    }

    private void logEvent(String msg) {
        logArea.appendText("[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + msg + "\n");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().addAll(root.getScene().getStylesheets());
        dialogPane.getStyleClass().add("dark-dialog");

        alert.initOwner(root.getScene().getWindow());
        alert.showAndWait();
    }

    private static String toHexColor(ir.fum.siliconvalley.model.enums.PlayerColor color) {
        return switch (color) {
            case BLUE -> "#38bdf8";
            case RED -> "#f87171";
            case GREEN -> "#4ade80";
            case YELLOW -> "#facc15";
        };
    }
}
