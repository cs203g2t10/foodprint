package foodprint.backend.exceptions;

public class UserUnverifiedException extends RuntimeException {
    public UserUnverifiedException() {
        super();
    }

    public UserUnverifiedException(String msg) {
        super(msg);
    }

    public UserUnverifiedException(Throwable cause) {
        super(cause);
    }

    public UserUnverifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
