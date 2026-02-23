package de.cocondo.app.system.entity.metadata;

import de.cocondo.app.system.dto.DomainEntityMetadataDTO;
import de.cocondo.app.system.entity.DomainEntity;
import de.cocondo.app.system.mapper.DomainMetadataSupportMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Generic metadata domain service for entities extending DomainEntity.
 *
 * Provides reusable logic for:
 * - reading metadata
 * - replacing metadata
 * - patching metadata
 *
 * Subclasses must implement entity loading and saving.
 */
@Transactional
public abstract class AbstractMetadataDomainService<T extends DomainEntity>
        implements DomainMetadataSupportMapper {

    protected final TagService tagService;
    protected final KeyValueService keyValueService;

    protected AbstractMetadataDomainService(
            TagService tagService,
            KeyValueService keyValueService
    ) {
        this.tagService = tagService;
        this.keyValueService = keyValueService;
    }

    protected abstract T loadEntity(String id);

    protected abstract T saveEntity(T entity);

    @Transactional(readOnly = true)
    public DomainEntityMetadataDTO getMetadataInternal(String id) {
        T entity = loadOrThrow(id);
        return toMetadata(entity);
    }

    public DomainEntityMetadataDTO replaceMetadataInternal(String id, DomainEntityMetadataDTO inbound) {

        T entity = loadOrThrow(id);

        entity.getTags().clear();

        if (inbound != null && inbound.getTags() != null) {
            for (String tag : inbound.getTags()) {
                if (tag != null && !tag.isBlank()) {
                    tagService.addTag(entity, tag.trim());
                }
            }
        }

        entity.getKeyValuePairs().clear();

        if (inbound != null && inbound.getKeyValuePairs() != null) {
            for (KeyValuePairDTO kv : inbound.getKeyValuePairs()) {
                if (kv == null) continue;
                String key = kv.getKey();
                if (key == null || key.isBlank()) continue;
                keyValueService.add(entity, key.trim(), kv.getValue());
            }
        }

        T persisted = saveEntity(entity);
        return toMetadata(persisted);
    }

    public DomainEntityMetadataDTO patchMetadataInternal(String id, DomainEntityMetadataDTO inbound) {

        T entity = loadOrThrow(id);

        if (inbound == null) {
            return toMetadata(entity);
        }

        Set<String> tags = inbound.getTags();
        if (tags != null) {
            entity.getTags().clear();
            for (String tag : tags) {
                if (tag != null && !tag.isBlank()) {
                    tagService.addTag(entity, tag.trim());
                }
            }
        }

        Set<KeyValuePairDTO> kvs = inbound.getKeyValuePairs();
        if (kvs != null) {
            entity.getKeyValuePairs().clear();
            for (KeyValuePairDTO kv : kvs) {
                if (kv == null) continue;
                String key = kv.getKey();
                if (key == null || key.isBlank()) continue;
                keyValueService.add(entity, key.trim(), kv.getValue());
            }
        }

        T persisted = saveEntity(entity);
        return toMetadata(persisted);
    }

    private T loadOrThrow(String id) {
        T entity = loadEntity(id);
        if (entity == null) {
            throw new EntityNotFoundException("Entity not found: " + id);
        }
        return entity;
    }
}
