package exception;

public class InvalidCellReferenceException extends RuntimeException {
    public InvalidCellReferenceException(String message) {
        super(message);
    }
}
