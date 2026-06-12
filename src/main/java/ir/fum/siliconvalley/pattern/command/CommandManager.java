package ir.fum.siliconvalley.pattern.command;

import ir.fum.siliconvalley.exception.GameException;
import ir.fum.siliconvalley.model.game.GameEngine;
import ir.fum.siliconvalley.pattern.memento.GameMemento;
import ir.fum.siliconvalley.pattern.memento.GameSnapshotCodec;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/** Command boundary with turn/action snapshot support for Undo/Redo. */
public final class CommandManager {
    private final GameEngine engine;
    private final GameSnapshotCodec codec;
    private final Deque<GameMemento> undoStack = new ArrayDeque<>();
    private final Deque<GameMemento> redoStack = new ArrayDeque<>();

    public CommandManager(GameEngine engine, GameSnapshotCodec codec) {
        this.engine = Objects.requireNonNull(engine, "engine");
        this.codec = Objects.requireNonNull(codec, "codec");
    }

    public void execute(GameCommand command) throws GameException {
        Objects.requireNonNull(command, "command");
        GameMemento before = codec.capture(engine.getGame());
        try {
            command.execute(engine);
            undoStack.push(before);
            redoStack.clear();
        } catch (GameException | RuntimeException exception) {
            engine.restore(codec.restore(before));
            throw exception;
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        redoStack.push(codec.capture(engine.getGame()));
        engine.restore(codec.restore(undoStack.pop()));
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }
        undoStack.push(codec.capture(engine.getGame()));
        engine.restore(codec.restore(redoStack.pop()));
    }
}
