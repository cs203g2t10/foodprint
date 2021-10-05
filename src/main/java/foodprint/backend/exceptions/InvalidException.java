package foodprint.backend.exceptions;

public class InvalidException extends RuntimeException {
    
    public InvalidException() {
        super();
    }

    public InvalidException(String msg) {
        super(msg);
    }

    public InvalidException(Throwable cause) {
        super(cause);
    }

    public InvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
