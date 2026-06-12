package ir.fum.siliconvalley.pattern.memento;

import ir.fum.siliconvalley.model.game.Game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class GameSnapshotCodec {
    public GameMemento capture(Game game) {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
             ObjectOutputStream output = new ObjectOutputStream(buffer)) {
            output.writeObject(game);
            output.flush();
            return new GameMemento(buffer.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Could not capture game snapshot", exception);
        }
    }

    public Game restore(GameMemento memento) {
        try (ByteArrayInputStream buffer = new ByteArrayInputStream(memento.serializedGame());
             ObjectInputStream input = new ObjectInputStream(buffer)) {
            Object value = input.readObject();
            if (!(value instanceof Game game)) {
                throw new IllegalStateException("Snapshot does not contain a Game object");
            }
            return game;
        } catch (IOException | ClassNotFoundException exception) {
            throw new IllegalStateException("Could not restore game snapshot", exception);
        }
    }
}
