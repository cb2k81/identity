package de.cocondo.app.domain.idm.person.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO representing an existing Person aggregate.
 *
 * Extends the payload DTO by adding the technical identifier.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PersonDTO extends PersonPayloadDTO {

    private String id;
}
