package de.cocondo.app.system.event;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorEntityRepository extends ListCrudRepository<ErrorEntity, Long> {

    @Query(value = "SELECT MAX(errorId) FROM ErrorEntity")
    Long findHighestId();

}
