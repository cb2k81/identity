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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Ensures consistent JSON error responses for authorization failures (403).
 *
 * Not used in your current path tests yet, but required for later method security tests.
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestAccessDeniedHandler.class);

    private final ObjectMapper objectMapper;
    private final EventPublisher eventPublisher;
    private final LocalMessageProvider errorMessageProvider;

    public RestAccessDeniedHandler(ObjectMapper objectMapper,
                                   EventPublisher eventPublisher,
                                   LocalMessageProvider errorMessageProvider) {
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.errorMessageProvider = errorMessageProvider;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        logger.warn("Forbidden request {} {} from IP {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                accessDeniedException.getMessage());

        RequestErrorEvent errorEvent =
                eventPublisher.publishRequestErrorEvent(this, accessDeniedException, request);

        String responseMessage =
                errorMessageProvider.getLocalizedErrorMessage(accessDeniedException, request);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                accessDeniedException.getClass().getName(),
                accessDeniedException.getMessage(),
                responseMessage,
                LocalDateTime.now(),
                errorEvent.getErrorId()
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}