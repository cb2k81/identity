package de.cocondo.app.domain.idm.role;

import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import de.cocondo.app.system.list.PagedQuerySupport;
import de.cocondo.app.system.list.PagedResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_ROLE_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListRolesPagedHandler {

    private final RoleEntityService roleEntityService;
    private final PagedQuerySupport pagedQuerySupport;
    private final PagedResponseFactory pagedResponseFactory;

    @PreAuthorize("hasAuthority('" + IDM_ROLE_READ + "')")
    public PagedResponseDTO<RoleDTO> handle(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String applicationScopeId,
            String name,
            Boolean systemProtected
    ) {

        pagedQuerySupport.validatePaging(page, size);

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = pagedQuerySupport.resolveSortDirection(sortDir);
        Specification<Role> specification = buildSpecification(applicationScopeId, name, systemProtected);

        Page<Role> result = roleEntityService.loadPage(
                specification,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        return pagedResponseFactory.fromPage(result, this::toDto);
    }

    private String resolveSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "name";
        }

        return switch (sortBy) {
            case "id", "name", "description", "systemProtected" -> sortBy;
            default -> throw new IllegalArgumentException("Unsupported sortBy for roles: " + sortBy);
        };
    }

    private Specification<Role> buildSpecification(
            String applicationScopeId,
            String name,
            Boolean systemProtected
    ) {
        Specification<Role> specification = Specification.where(null);

        if (applicationScopeId != null && !applicationScopeId.isBlank()) {
            specification = specification.and(
                    (root, query, cb) -> cb.equal(root.get("applicationScope").get("id"), applicationScopeId.trim())
            );
        }

        if (name != null && !name.isBlank()) {
            specification = specification.and(
                    (root, query, cb) -> cb.like(
                            cb.lower(root.get("name")),
                            "%" + name.trim().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        if (systemProtected != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.equal(root.get("systemProtected"), systemProtected)
            );
        }

        return specification;
    }

    private RoleDTO toDto(Role role) {

        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setApplicationScopeId(role.getApplicationScope().getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setSystemProtected(role.isSystemProtected());

        return dto;
    }
}