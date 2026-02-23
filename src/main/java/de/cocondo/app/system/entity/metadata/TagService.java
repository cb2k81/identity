package de.cocondo.app.system.entity.metadata;

import de.cocondo.app.system.entity.DomainEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class TagService {
    private static final Logger logger = LoggerFactory.getLogger(TagService.class);

    public void addTag(DomainEntity entity, String tag) {
        logger.info("Adding tag='{}' to {}", tag, entity.getClass().getSimpleName());
        if (entity.getTags() == null) {
            entity.setTags(new HashSet<>());
        }
        entity.getTags().add(tag);
    }

    public void removeTag(DomainEntity entity, String tag) {
        logger.info("Removing tag='{}' from {}", tag, entity.getClass().getSimpleName());
        if (entity.getTags() != null) {
            entity.getTags().remove(tag);
        }
    }

    public Set<String> getAllTags(DomainEntity entity) {
        logger.info("Retrieving all tags for {}", entity.getClass().getSimpleName());
        return entity.getTags() != null ? entity.getTags() : new HashSet<>();
    }

    public boolean hasTag(DomainEntity entity, String tag) {
        logger.info("Checking if tag='{}' exists in {}", tag, entity.getClass().getSimpleName());
        return entity.getTags() != null && entity.getTags().contains(tag);
    }
}
