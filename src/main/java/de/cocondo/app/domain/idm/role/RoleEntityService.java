package de.cocondo.app.domain.idm.role;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    public boolean existsByApplicationScopeId(String applicationScopeId) {
        return roleRepository.existsByApplicationScope_Id(applicationScopeId);
    }

    public Page<Role> loadPage(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    public Page<Role> loadPage(Specification<Role> specification, Pageable pageable) {
        return roleRepository.findAll(specification, pageable);
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