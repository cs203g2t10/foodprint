package foodprint.backend.exceptions;

public class DeleteFailedException extends RuntimeException {
    
    public DeleteFailedException() {
        super();
    }

    public DeleteFailedException(String msg) {
        super(msg);
    }

    public DeleteFailedException(Throwable cause) {
        super(cause);
    }

    public DeleteFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
