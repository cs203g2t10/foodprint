package foodprint.backend.exceptions;

public class VaccinationValidationException extends RuntimeException {

    public VaccinationValidationException() {
        super();
    }

    public VaccinationValidationException(String msg) {
        super(msg);
    }

    public VaccinationValidationException(Throwable cause) {
        super(cause);
    }

    public VaccinationValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
