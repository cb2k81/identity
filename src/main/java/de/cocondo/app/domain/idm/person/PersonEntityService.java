package de.cocondo.app.domain.idm.person;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Entity service for the Person aggregate.
 *
 * This service encapsulates persistence access for Person entities
 * and operates exclusively on domain objects.
 *
 * Responsibilities:
 * - loading Person aggregates
 * - persisting Person aggregates
 *
 * This service:
 * - knows no DTOs
 * - knows no permissions
 * - contains no use-case orchestration
 */
@Service
@RequiredArgsConstructor
public class PersonEntityService {

    private final PersonRepository personRepository;

    /**
     * Loads a Person aggregate by its technical identifier.
     *
     * @param id technical identifier of the Person
     * @return optional Person aggregate
     */
    public Optional<Person> loadById(String id) {
        return personRepository.findById(id);
    }

    /**
     * Persists the given Person aggregate.
     *
     * @param person Person aggregate to persist
     * @return persisted Person aggregate
     */
    public Person save(Person person) {
        return personRepository.save(person);
    }
}
