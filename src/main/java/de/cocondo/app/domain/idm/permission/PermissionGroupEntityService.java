package de.cocondo.app.domain.idm.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Entity service for PermissionGroup.
 *
 * Encapsulates persistence access and operates exclusively on domain entities.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionGroupEntityService {

    private final PermissionGroupRepository permissionGroupRepository;

    public Optional<PermissionGroup> loadById(String id) {
        return permissionGroupRepository.findById(id);
    }

    public Optional<PermissionGroup> loadByApplicationScopeIdAndName(String applicationScopeId, String name) {
        return permissionGroupRepository.findByApplicationScope_IdAndName(applicationScopeId, name);
    }

    @Transactional
    public PermissionGroup save(PermissionGroup group) {
        return permissionGroupRepository.save(group);
    }
}