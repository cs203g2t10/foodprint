package foodprint.backend.exceptions;

public class InsufficientPermissionsException extends RuntimeException {
    
    public InsufficientPermissionsException() {
        super();
    }

    public InsufficientPermissionsException(String msg) {
        super(msg);
    }

    public InsufficientPermissionsException(Throwable cause) {
        super(cause);
    }

    public InsufficientPermissionsException(String message, Throwable cause) {
        super(message, cause);
    }

}
