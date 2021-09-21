package foodprint.backend.exceptions;

public class AlreadyExistsException extends RuntimeException {
    
    public AlreadyExistsException() {
        super();
    }

    public AlreadyExistsException(String msg) {
        super(msg);
    }

    public AlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public AlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

}
