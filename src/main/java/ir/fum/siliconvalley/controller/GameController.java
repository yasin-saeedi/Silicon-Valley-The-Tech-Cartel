package ir.fum.siliconvalley.controller;

import ir.fum.siliconvalley.model.game.Game;
import ir.fum.siliconvalley.model.game.GameEngine;
import ir.fum.siliconvalley.pattern.command.CommandManager;
import ir.fum.siliconvalley.pattern.command.GameCommand;
import ir.fum.siliconvalley.persistence.GameSaveService;
import javafx.application.Platform;

import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/** Keeps game mutations away from the JavaFX application thread. */
public final class GameController implements AutoCloseable {
    private final GameEngine engine;
    private final CommandManager commandManager;
    private final GameSaveService saveService;
    private final ExecutorService gameExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "game-logic-worker");
        thread.setDaemon(true);
        return thread;
    });

    public GameController(GameEngine engine, CommandManager commandManager, GameSaveService saveService) {
        this.engine = Objects.requireNonNull(engine, "engine");
        this.commandManager = Objects.requireNonNull(commandManager, "commandManager");
        this.saveService = Objects.requireNonNull(saveService, "saveService");
    }

    public void execute(GameCommand command, Runnable onSuccess, Consumer<Throwable> onFailure) {
        gameExecutor.submit(() -> {
            try {
                commandManager.execute(command);
                Platform.runLater(onSuccess);
            } catch (Throwable throwable) {
                Platform.runLater(() -> onFailure.accept(throwable));
            }
        });
    }

    public void save(Path targetFile, Runnable onSuccess, Consumer<Throwable> onFailure) {
        saveService.save(engine.getGame(), targetFile)
                .thenRun(() -> Platform.runLater(onSuccess))
                .exceptionally(throwable -> {
                    Platform.runLater(() -> onFailure.accept(throwable));
                    return null;
                });
    }

    public void load(Path sourceFile, Runnable onSuccess, Consumer<Throwable> onFailure) {
        saveService.load(sourceFile)
                .thenAccept(game -> {
                    engine.restore(game);
                    Platform.runLater(onSuccess);
                })
                .exceptionally(throwable -> {
                    Platform.runLater(() -> onFailure.accept(throwable));
                    return null;
                });
    }

    public Game getGame() {
        return engine.getGame();
    }

    public void undo(Runnable onSuccess) {
        gameExecutor.submit(() -> {
            commandManager.undo();
            Platform.runLater(onSuccess);
        });
    }

    public void redo(Runnable onSuccess) {
        gameExecutor.submit(() -> {
            commandManager.redo();
            Platform.runLater(onSuccess);
        });
    }

    @Override
    public void close() {
        gameExecutor.shutdownNow();
        saveService.close();
    }
}
