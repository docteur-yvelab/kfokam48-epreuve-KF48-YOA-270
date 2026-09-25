package com.kfokam48.presence.exception;

import com.kfokam48.presence.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        HttpStatus status = switch (ex.getCode()) {
            case "CODE_INCONNU" -> HttpStatus.BAD_REQUEST;
            case "CODE_EXPIRE" -> HttpStatus.GONE;
            case "DEJA_PRESENT", "EXERCICE_DEJA_DEPOSE", "RELECTURE_DEJA_RENDUE" -> HttpStatus.CONFLICT;
            case "NOTE_INVALIDE", "LIEN_INVALIDE" -> HttpStatus.BAD_REQUEST;
            case "AUTO_RELECTURE" -> HttpStatus.FORBIDDEN;
            case "PROMOTION_INCONNUE" -> HttpStatus.NOT_FOUND;
            case "SESSION_CLOTUREE", "RELECTURE_NON_MODIFIABLE" -> HttpStatus.CONFLICT;
            case "AUCUNE_RELECTURE", "AUCUN_ETUDIANT_PRESENT" -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(cv -> cv.getMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse("ERREUR_INTERNE", "Une erreur inattendue est survenue."));
    }
}