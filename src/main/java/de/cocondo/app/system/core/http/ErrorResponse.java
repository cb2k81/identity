package de.cocondo.app.system.core.http;

import jakarta.validation.constraints.Null;

import java.time.LocalDateTime;

public class ErrorResponse {
    private int status;
    private String errorType;
    private String message;
    private String localMessage;
    private LocalDateTime timestamp;
    private Long errorId;

    public ErrorResponse(int status, String errorType, String message, String localMessage, LocalDateTime timestamp, Long errorId) {
        this.status = status;
        this.errorType = errorType;
        this.message = message;
        this.localMessage = localMessage;
        this.timestamp = timestamp;
        this.errorId = errorId;
    }

    public Long getErrorId() {
        return errorId;
    }

    public void setErrorId(Long errorId) {
        this.errorId = errorId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLocalMessage() {
        return localMessage;
    }

    public void setLocalMessage(String localMessage) {
        this.localMessage = localMessage;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
