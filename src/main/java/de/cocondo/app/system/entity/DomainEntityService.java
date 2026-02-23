package de.cocondo.app.system.entity;

import de.cocondo.app.system.core.id.IdGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DomainEntityService {

    private final IdGeneratorService idGeneratorService;


    public <T extends DomainEntity> T createNewEntity(Class<T> entityClass) {
        try {
            T entity = entityClass.getDeclaredConstructor().newInstance();

            entity.setId(idGeneratorService.generateId());
            return entity;
        } catch (Exception e) {
            throw new IllegalStateException("Error creating entity of class " + entityClass, e);
        }
    }
}

