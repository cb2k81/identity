package de.cocondo.app.domain.idm.role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleEntityService {

    private final RoleRepository roleRepository;

    public Optional<Role> loadById(String id) {
        return roleRepository.findById(id);
    }

    public Optional<Role> loadByApplicationScopeIdAndName(String applicationScopeId, String name) {
        return roleRepository.findByApplicationScope_IdAndName(applicationScopeId, name);
    }

    @Transactional
    public Role save(Role role) {
        Objects.requireNonNull(role, "role must not be null");
        return roleRepository.save(role);
    }

    @Transactional
    public void delete(Role role) {
        roleRepository.delete(role);
    }
}