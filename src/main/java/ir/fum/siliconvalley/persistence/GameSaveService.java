package ir.fum.siliconvalley.persistence;

import ir.fum.siliconvalley.model.game.Game;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public interface GameSaveService extends AutoCloseable {
    CompletableFuture<Void> save(Game game, Path targetFile);

    CompletableFuture<Game> load(Path sourceFile);

    @Override
    void close();
}
