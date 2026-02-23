package de.cocondo.app.system.core.http;

import de.cocondo.app.system.event.ErrorEvent;
import jakarta.servlet.http.HttpServletRequest;

public class RequestErrorEvent extends ErrorEvent {

    private final String requestMethod;
    private final String requestUri;
    private final String remoteAddress;

    private final HttpServletRequest request;

    public RequestErrorEvent(
            Object source,
            Exception exception,
            HttpServletRequest request,
            Long errorId) {
        super(source, exception, errorId);
        this.request = request;
        this.requestMethod = request.getMethod();
        this.requestUri = request.getRequestURI();
        this.remoteAddress = request.getRemoteAddr();

    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

}
