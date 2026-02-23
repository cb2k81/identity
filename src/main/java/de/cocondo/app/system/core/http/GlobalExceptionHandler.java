package de.cocondo.app.system.core.http;

import de.cocondo.app.system.core.locale.LocalMessageProvider;
import de.cocondo.app.system.event.EventPublisher;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final EventPublisher eventPublisher;
    private final LocalMessageProvider errorMessageProvider;

    public GlobalExceptionHandler(EventPublisher eventPublisher, LocalMessageProvider errorMessageProvider) {
        this.eventPublisher = eventPublisher;
        this.errorMessageProvider = errorMessageProvider;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(HttpServletRequest request, NoHandlerFoundException ex) {
        return logAndCreateErrorResponse(request, ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(HttpServletRequest request, EntityNotFoundException ex) {
        return logAndCreateErrorResponse(request, ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException ex) {
        return logAndCreateErrorResponse(request, ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidConfigurationPropertyValueException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleInvalidConfigurationPropertyValueException(HttpServletRequest request, InvalidConfigurationPropertyValueException ex) {
        return logAndCreateErrorResponse(request, ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException ex) {
        return logAndCreateErrorResponse(request, ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(HttpServletRequest request, MethodArgumentNotValidException ex) {
        return logAndCreateErrorResponse(request, ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleValidationException(HttpServletRequest request, ValidationException ex) {
        return logAndCreateErrorResponse(request, ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(HttpServletRequest request, AccessDeniedException ex) {
        return logAndCreateErrorResponse(request, ex, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleAuthenticationException(HttpServletRequest request, AuthenticationException ex) {
        return logAndCreateErrorResponse(request, ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailureException(HttpServletRequest request, OptimisticLockingFailureException ex) {
        return logAndCreateErrorResponse(request, ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request, Exception ex) {
        return logAndCreateErrorResponse(request, ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> logAndCreateErrorResponse(HttpServletRequest request, Exception ex, HttpStatus status) {
        String message = String.format("Exception occurred for request %s %s from IP %s: %s",
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr(), ex.getMessage());

        logger.error(message, ex);

        if (logger.isDebugEnabled()) {
            logRequestDetails(request);
        }

        logFullExceptionDetails(ex);

        RequestErrorEvent errorEvent = eventPublisher.publishRequestErrorEvent(this, ex, request);

        String responseMessage = errorMessageProvider.getLocalizedErrorMessage(ex, request);

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                ex.getClass().getName(),
                ex.getMessage(),
                responseMessage,
                LocalDateTime.now(),
                errorEvent.getErrorId()
        );

        return new ResponseEntity<>(errorResponse, status);
    }

    private void logFullExceptionDetails(Exception ex) {
        Throwable cause = ex;
        while (cause != null) {
            logger.error("Caused by: " + cause.getMessage(), cause);
            cause = cause.getCause();
        }
    }

    private void logRequestDetails(HttpServletRequest request) {
        StringBuilder requestDetails = new StringBuilder();
        requestDetails.append("Request Details:\n");
        requestDetails.append("=====================\n");
        requestDetails.append("[Request Method] : ").append(request.getMethod()).append("\n");
        requestDetails.append("[Request URI] : ").append(request.getRequestURI()).append("\n");
        requestDetails.append("[Remote Address] : ").append(request.getRemoteAddr()).append("\n");

        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!parameterMap.isEmpty()) {
            requestDetails.append("[Request Parameters] : ");
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String parameterName = entry.getKey();
                String[] parameterValues = entry.getValue();
                requestDetails.append(parameterName).append("=").append(Arrays.toString(parameterValues)).append(", ");
            }
            requestDetails.setLength(requestDetails.length() - 2);
            requestDetails.append("\n");
        }

        logger.debug(requestDetails.toString());
    }
}
