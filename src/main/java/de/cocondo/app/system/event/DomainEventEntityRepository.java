package de.cocondo.app.system.event;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DomainEventEntityRepository extends ListCrudRepository<DomainEventEntity, Long> {

}
