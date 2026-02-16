package de.cocondo.app.domain.idm.person;

import de.cocondo.app.domain.idm.person.dto.PersonDTO;
import de.cocondo.app.domain.idm.person.dto.PersonPayloadDTO;
import de.cocondo.app.domain.idm.person.mapper.PersonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain service for the Person aggregate.
 *
 * This service represents the second service layer (business / use-case layer).
 * It orchestrates entity services and mappers and works with DTOs at its boundary.
 *
 * Responsibilities:
 * - implement person-related use cases
 * - define transactional boundaries
 * - convert between DTOs and domain entities
 *
 * This service:
 * - knows DTOs and domain entities
 * - does NOT perform technical security checks yet
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PersonDomainService {

    private final PersonEntityService personEntityService;
    private final PersonMapper personMapper;

    /**
     * Creates a new Person aggregate from the given payload DTO.
     *
     * @param payloadDTO data required to create a person
     * @return created Person as DTO
     */
    public PersonDTO createPerson(PersonPayloadDTO payloadDTO) {

        Person person = personMapper.fromPayload(payloadDTO);

        // ID generation is an application concern, not a mapping concern
        person.setId(UUID.randomUUID().toString());

        Person persisted = personEntityService.save(person);

        return personMapper.toDto(persisted);
    }

    /**
     * Loads an existing Person aggregate by its technical identifier.
     *
     * @param id technical identifier of the person
     * @return optional PersonDTO
     */
    @Transactional(readOnly = true)
    public Optional<PersonDTO> getPersonById(String id) {

        return personEntityService
                .loadById(id)
                .map(personMapper::toDto);
    }
}
