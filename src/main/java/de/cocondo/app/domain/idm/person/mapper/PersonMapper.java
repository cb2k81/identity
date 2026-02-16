package de.cocondo.app.domain.idm.person.mapper;

import de.cocondo.app.domain.idm.person.Person;
import de.cocondo.app.domain.idm.person.dto.PersonDTO;
import de.cocondo.app.domain.idm.person.dto.PersonPayloadDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for the Person aggregate.
 *
 * This mapper transforms:
 * - Person entities to DTOs
 * - payload DTOs to Person entities
 *
 * It contains no business logic and performs no ID generation.
 */
@Mapper(componentModel = "spring")
public interface PersonMapper {

    PersonDTO toDto(Person person);

    @Mapping(target = "id", ignore = true)
    Person fromPayload(PersonPayloadDTO payloadDTO);
}
