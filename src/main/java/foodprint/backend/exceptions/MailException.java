package foodprint.backend.exceptions;

public class MailException extends RuntimeException {
    
    public MailException() {
        super();
    }

    public MailException(String msg) {
        super(msg);
    }

    public MailException(Throwable cause) {
        super(cause);
    }

    public MailException(String message, Throwable cause) {
        super(message, cause);
    }
}
