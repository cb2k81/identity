package de.cocondo.app.system.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.cocondo.app.system.entity.metadata.KeyValuePairDTO;
import jakarta.validation.ValidationException;
import lombok.Data;

import java.io.Serial;
import java.util.Set;

@Data
abstract public class DomainEntityInboundDTO implements DTO {
    @Serial
    private static final long serialVersionUID = 1L;
    private Set<String> tags;
    private Set<KeyValuePairDTO> keyValuePairs;

    public void validate() {
    }

    @JsonIgnore
    public boolean isValid() {
        try {
            validate();
            return true;
        } catch(ValidationException e) {
            return false;
        }
    }

}
