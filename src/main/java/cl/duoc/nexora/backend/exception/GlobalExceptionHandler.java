package cl.duoc.nexora.backend.exception;

import cl.duoc.nexora.backend.dto.error.ApiErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errores = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), safeMessage(error.getDefaultMessage(), "Valor invalido"))
        );
        exception.getBindingResult().getGlobalErrors().forEach(error ->
                errores.put(error.getObjectName(), safeMessage(error.getDefaultMessage(), "Valor invalido"))
        );

        return buildResponse(HttpStatus.BAD_REQUEST, request, "Datos invalidos", errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> errores = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                errores.put(violation.getPropertyPath().toString(), safeMessage(violation.getMessage(), "Valor invalido"))
        );

        return buildResponse(HttpStatus.BAD_REQUEST, request, "Datos invalidos", errores);
    }

    @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            RuntimeException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, request, safeMessage(exception.getMessage(), "Recurso no encontrado"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, request, safeMessage(exception.getMessage(), "Solicitud invalida"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                request,
                "No se pudo completar la operacion por una restriccion de datos"
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, request, "No autenticado");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.FORBIDDEN, request, "Acceso denegado");
    }

    @ExceptionHandler(MlServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleMlService(
            MlServiceException exception,
            HttpServletRequest request
    ) {
        return buildResponse(
                exception.getStatus(),
                request,
                safeMessage(exception.getMessage(), "Error en el servicio de IA")
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(
            Exception exception,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, request, "Error interno del servidor");
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            HttpServletRequest request,
            String mensaje
    ) {
        return buildResponse(status, request, mensaje, null);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            HttpServletRequest request,
            String mensaje,
            Map<String, String> errores
    ) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                status.value(),
                mensaje,
                request.getRequestURI(),
                LocalDateTime.now(),
                errores
        ));
    }

    private String safeMessage(String message, String fallback) {
        if (message == null || message.isBlank()) {
            return fallback;
        }
        return message;
    }
}
