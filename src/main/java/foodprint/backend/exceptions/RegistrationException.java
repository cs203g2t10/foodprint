package foodprint.backend.exceptions;

public class RegistrationException extends RuntimeException {
    public RegistrationException() {
        super();
    }

    public RegistrationException(String msg) {
        super(msg);
    }

    public RegistrationException(Throwable cause) {
        super(cause);
    }

    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
