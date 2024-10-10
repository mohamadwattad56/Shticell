package exception;

public class EmptyCellException extends RuntimeException {
    public EmptyCellException() {
        super();
    }

    public EmptyCellException(String message) {
        super(message);
    }
}
