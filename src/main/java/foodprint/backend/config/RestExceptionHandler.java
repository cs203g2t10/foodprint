package foodprint.backend.config;

import com.stripe.exception.StripeException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import foodprint.backend.exceptions.AlreadyExistsException;
import foodprint.backend.exceptions.DeleteFailedException;
import foodprint.backend.exceptions.InsufficientPermissionsException;
import foodprint.backend.exceptions.MailException;
import foodprint.backend.exceptions.NotFoundException;
import foodprint.backend.exceptions.RegistrationException;
import foodprint.backend.exceptions.VaccinationValidationException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpHeaders headers,
        HttpStatus status,
        WebRequest request ) {

        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        List<String> messages = new ArrayList<>();
        for (ObjectError objectError : ex.getBindingResult().getAllErrors()){
            if (objectError instanceof FieldError) {
                FieldError fieldError = (FieldError) objectError;
                messages.add(fieldError.getField() + " " + fieldError.getDefaultMessage());
            } else {
                messages.add(objectError.getDefaultMessage());
            }
        }
        Collections.sort(messages);
        body.put("message", messages);
        body.put("path", request.getDescription(false));
        return new ResponseEntity<>(body, headers, status);
    }

    /**
     * Handle the case in which arguments for controller's methods did not match the type.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public void handleTypeMismatch(HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Handle when a NotFoundException is thrown
     */
    @ExceptionHandler(NotFoundException.class)
    public void handleNotFound(NotFoundException ex, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler(DeleteFailedException.class)
    public void handleNotFound(DeleteFailedException ex, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }

    @ExceptionHandler(InsufficientPermissionsException.class)
    public void handleInsufficientPermissions(InsufficientPermissionsException ex, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    }
    
    @ExceptionHandler(AlreadyExistsException.class)
    public void handleConflict(AlreadyExistsException ex, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.CONFLICT.value(), ex.getMessage());
    }

     @ExceptionHandler(StripeException.class)
     public void handleStripeError(StripeException ex, HttpServletResponse response) throws IOException{
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value() ,ex.getMessage());
    }

    @ExceptionHandler(MailException.class)
    public void handleMailError(MailException ex, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }

    @ExceptionHandler(VaccinationValidationException.class)
    public void handleVaccinationExceptionError(VaccinationValidationException ex, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }

    @ExceptionHandler(RegistrationException.class)
    public void handleVaccinationExceptionError(RegistrationException ex, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage());
    }
}
