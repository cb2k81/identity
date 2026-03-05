package de.cocondo.app.system.core.http;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.NoHandlerFoundException;

@Component
public class DefaultFallbackInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Wenn Spring einen Handler gefunden hat, ist es *kein* Fallback.
        if (handler instanceof HandlerMethod) {
            return true;
        }

        // Optional: wenn du statische Ressourcen auch nicht als "fehlend" behandeln willst:
        // if (handler instanceof org.springframework.web.servlet.resource.ResourceHttpRequestHandler) { return true; }

        // Nur dann "No endpoint", wenn wirklich kein Controller-Handler matched.
        throw new NoHandlerFoundException(
                request.getMethod(),
                request.getRequestURI(),
                new HttpHeaders()
        );
    }
}