package de.cocondo.app.system.event;

import org.springframework.context.ApplicationEvent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

public class ErrorEvent extends ApplicationEvent {

    private final String errorMessage;
    private final Long errorId;
    private final String errorType;
    private final String stackTrace;

    public ErrorEvent(Object source, Exception exception, Long errorId) {
        super(source);
        this.errorMessage = exception.getMessage();
        this.errorId = errorId;
        this.errorType = exception.getClass().getName();
        this.stackTrace = getStackTraceAsString(exception);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Long getErrorId() {
        return errorId;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    private static String getStackTraceAsString(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
}
