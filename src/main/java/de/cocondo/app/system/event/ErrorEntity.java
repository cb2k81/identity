package de.cocondo.app.system.event;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Data;

@Data
@Entity
@Table(name = "error_events")
public class ErrorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String errorType;

    @Column(columnDefinition = "TEXT")
    @Lob
    private String errorMessage;
    private Long errorId;
    private LocalDateTime timestamp;
    @Column(columnDefinition = "LONGTEXT")
    @Lob
    private String stackTrace;

    private String requestMethod;
    private String requestUri;
    private String remoteAddress;

    private String principalId;

    // Neues Feld für den Klassennamen des gemappten Objekts
    private String mappedObjectClassName;

    @Column(columnDefinition = "LONGTEXT")
    @Lob
    private String payload;

    public ErrorEntity() {
        this.timestamp = LocalDateTime.now();
    }
}
