package de.cocondo.app.domain.idm.person;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing and persisting the Person aggregate.
 *
 * Requirements covered by JpaRepository:
 * - CRUD operations
 * - pagination and sorting (via PagingAndSortingRepository)
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, String> {
}
