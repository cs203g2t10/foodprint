package foodprint.backend.exceptions;

public class BadRequestException extends RuntimeException{
    public BadRequestException() {
        super();
    }

    public BadRequestException(String msg) {
        super(msg);
    }

    public BadRequestException(Throwable cause) {
        super(cause);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
