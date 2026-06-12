package ir.fum.siliconvalley.persistence;

import ir.fum.siliconvalley.exception.CorruptedSaveFileException;
import ir.fum.siliconvalley.model.game.Game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** File operations run outside the JavaFX application thread. */
public final class JavaSerializationGameSaveService implements GameSaveService {
    private final ExecutorService fileExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "game-save-loader");
        thread.setDaemon(true);
        return thread;
    });

    @Override
    public CompletableFuture<Void> save(Game game, Path targetFile) {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(targetFile, "targetFile");
        return CompletableFuture.runAsync(() -> {
            try {
                Path parent = targetFile.toAbsolutePath().getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(targetFile))) {
                    output.writeObject(game);
                }
            } catch (IOException exception) {
                throw new CompletionException(exception);
            }
        }, fileExecutor);
    }

    @Override
    public CompletableFuture<Game> load(Path sourceFile) {
        Objects.requireNonNull(sourceFile, "sourceFile");
        return CompletableFuture.supplyAsync(() -> {
            try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(sourceFile))) {
                Object value = input.readObject();
                if (!(value instanceof Game game)) {
                    throw new IOException("File does not contain a Game object");
                }
                return game;
            } catch (IOException | ClassNotFoundException exception) {
                throw new CompletionException(new CorruptedSaveFileException("Save file is damaged or incomplete", exception));
            }
        }, fileExecutor);
    }

    @Override
    public void close() {
        fileExecutor.shutdownNow();
    }
}
