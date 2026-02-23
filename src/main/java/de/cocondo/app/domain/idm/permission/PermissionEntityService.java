package de.cocondo.app.domain.idm.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Entity service for Permission.
 *
 * Encapsulates persistence access and operates exclusively on domain entities.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionEntityService {

    private final PermissionRepository permissionRepository;

    public Optional<Permission> loadById(String id) {
        return permissionRepository.findById(id);
    }

    public Optional<Permission> loadByApplicationScopeIdAndName(String applicationScopeId, String name) {
        return permissionRepository.findByApplicationScope_IdAndName(applicationScopeId, name);
    }

    public List<Permission> loadAllByApplicationScopeId(String applicationScopeId) {
        return permissionRepository.findAllByApplicationScope_Id(applicationScopeId);
    }

    @Transactional
    public Permission save(Permission permission) {
        return permissionRepository.save(permission);
    }
}