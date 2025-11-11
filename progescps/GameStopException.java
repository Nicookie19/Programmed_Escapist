package progescps;

public class GameStopException extends RuntimeException {
    public GameStopException() {
        super("Game stopped");
    }
    
    public GameStopException(String message) {
        super(message);
    }
}