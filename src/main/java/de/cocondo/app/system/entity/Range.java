package de.cocondo.app.system.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Embeddable
public class Range {
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}
