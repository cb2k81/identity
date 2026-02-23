package de.cocondo.app.system.dto;

import de.cocondo.app.system.entity.metadata.KeyValuePairDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class DomainEntityMetadataDTO {
    private String id;
    private String createdBy;
    private LocalDateTime createdAt;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedAt;
    private Set<String> tags;
    private Set<KeyValuePairDTO> keyValuePairs;
}
