package ir.fum.siliconvalley.pattern.memento;

import java.util.Arrays;

public record GameMemento(byte[] serializedGame) {
    public GameMemento {
        serializedGame = Arrays.copyOf(serializedGame, serializedGame.length);
    }

    @Override
    public byte[] serializedGame() {
        return Arrays.copyOf(serializedGame, serializedGame.length);
    }
}
