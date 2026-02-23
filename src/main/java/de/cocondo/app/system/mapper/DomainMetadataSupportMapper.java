package de.cocondo.app.system.mapper;

import de.cocondo.app.system.dto.DomainEntityMetadataDTO;
import de.cocondo.app.system.entity.DomainEntity;
import de.cocondo.app.system.entity.metadata.KeyValuePairDTO;

import java.util.stream.Collectors;

/**
 * Interface for mapping between {@link DomainEntity} objects and their corresponding
 * {@link DomainEntityMetadataDTO} representations. This provides functionality to convert
 * a domain entity into a metadata DTO containing relevant metadata attributes.
 */
public interface DomainMetadataSupportMapper {

    default DomainEntityMetadataDTO toMetadata(DomainEntity entity) {
        if (entity == null) return null;

        DomainEntityMetadataDTO dto = new DomainEntityMetadataDTO();
        dto.setId(entity.getId());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedAt(entity.getLastModifiedAt());
        dto.setTags(entity.getTags());

        if (entity.getKeyValuePairs() != null) {
            dto.setKeyValuePairs(
                    entity.getKeyValuePairs().stream().map(kv -> {
                        KeyValuePairDTO d = new KeyValuePairDTO();
                        d.setKey(kv.getKey());
                        d.setValue(kv.getValue());
                        return d;
                    }).collect(Collectors.toSet())
            );
        }

        return dto;
    }
}
