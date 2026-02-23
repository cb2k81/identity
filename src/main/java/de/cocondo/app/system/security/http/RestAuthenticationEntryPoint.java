package de.cocondo.app.system.security.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.cocondo.app.system.core.http.ErrorResponse;
import de.cocondo.app.system.core.http.RequestErrorEvent;
import de.cocondo.app.system.core.locale.LocalMessageProvider;
import de.cocondo.app.system.event.EventPublisher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Ensures consistent JSON error responses for authentication failures (401),
 * e.g. missing/invalid token on /api/**.
 *
 * This runs BEFORE controller invocation, therefore GlobalExceptionHandler is NOT involved.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;
    private final LocalMessageProvider errorMessageProvider;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper,
                                        EventPublisher eventPublisher,
                                        LocalMessageProvider errorMessageProvider) {
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.errorMessageProvider = errorMessageProvider;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        logger.warn("Unauthorized request {} {} from IP {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                authException.getMessage());

        RequestErrorEvent errorEvent =
                eventPublisher.publishRequestErrorEvent(this, authException, request);

        String responseMessage =
                errorMessageProvider.getLocalizedErrorMessage(authException, request);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                authException.getClass().getName(),
                authException.getMessage(),
                responseMessage,
                LocalDateTime.now(),
                errorEvent.getErrorId()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}