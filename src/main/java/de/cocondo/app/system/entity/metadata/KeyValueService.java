package de.cocondo.app.system.entity.metadata;

import de.cocondo.app.system.entity.DomainEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KeyValueService {
    private static final Logger logger = LoggerFactory.getLogger(KeyValueService.class);

    public void add(DomainEntity entity, String key, String value) {
        logger.info("Adding metadata: key='{}', value='{}' to entity {} (ID={})", key, value, entity.getClass().getSimpleName(), entity.getId());
        entity.addKeyValue(key, value);
    }

    public void remove(DomainEntity entity, String key) {
        logger.info("Removing metadata: key='{}' from entity {} (ID={})", key, entity.getClass().getSimpleName(), entity.getId());
        entity.removeKeyValue(key);
    }

    public String getValue(DomainEntity entity, String key) {
        logger.info("Retrieving metadata: key='{}' from entity {} (ID={})", key, entity.getClass().getSimpleName(), entity.getId());
        return entity.getValueByKey(key);
    }

    public Map<String, String> getAll(DomainEntity entity) {
        logger.info("Retrieving all metadata for entity {} (ID={})", entity.getClass().getSimpleName(), entity.getId());
        return entity.getAllKeyValues().stream()
                .collect(Collectors.toMap(KeyValuePair::getKey, KeyValuePair::getValue));
    }
}
