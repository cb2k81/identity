package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.permission.dto.PermissionDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import de.cocondo.app.system.list.PagedQuerySupport;
import de.cocondo.app.system.list.PagedResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_ROLE_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListPermissionsOfRolePagedHandler {

    private final RolePermissionAssignmentEntityService rolePermissionAssignmentEntityService;
    private final PagedQuerySupport pagedQuerySupport;
    private final PagedResponseFactory pagedResponseFactory;

    @PreAuthorize("hasAuthority('" + IDM_ROLE_READ + "')")
    public PagedResponseDTO<PermissionDTO> handle(
            String roleId,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        if (roleId == null || roleId.isBlank()) {
            throw new IllegalArgumentException("roleId must not be blank");
        }

        pagedQuerySupport.validatePaging(page, size);

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = pagedQuerySupport.resolveSortDirection(sortDir);

        Page<RolePermissionAssignment> result = rolePermissionAssignmentEntityService.loadPageByRoleId(
                roleId,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        return pagedResponseFactory.fromPage(result, assignment -> {
            PermissionDTO dto = new PermissionDTO();
            dto.setId(assignment.getPermission().getId());
            dto.setApplicationScopeId(assignment.getPermission().getApplicationScope().getId());
            dto.setPermissionGroupId(assignment.getPermission().getPermissionGroup().getId());
            dto.setName(assignment.getPermission().getName());
            dto.setDescription(assignment.getPermission().getDescription());
            dto.setSystemProtected(assignment.getPermission().isSystemProtected());
            return dto;
        });
    }

    private String resolveSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "permission.name";
        }

        return switch (sortBy) {
            case "id" -> "permission.id";
            case "name" -> "permission.name";
            case "description" -> "permission.description";
            case "systemProtected" -> "permission.systemProtected";
            default -> throw new IllegalArgumentException("Unsupported sortBy for role permissions: " + sortBy);
        };
    }
}