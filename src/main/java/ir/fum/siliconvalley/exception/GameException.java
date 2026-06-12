package ir.fum.siliconvalley.exception;

/** Base checked exception for recoverable game-rule errors. */
public class GameException extends Exception {
    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
}
