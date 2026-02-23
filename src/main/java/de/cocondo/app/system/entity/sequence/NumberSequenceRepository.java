package de.cocondo.app.system.entity.sequence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NumberSequenceRepository extends CrudRepository<NumberSequence, String> {
    NumberSequence findBySequenceName(String sequenceName);
}
