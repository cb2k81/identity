package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.role.dto.RoleDTO;
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

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_PERMISSION_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListRolesOfPermissionPagedHandler {

    private final RolePermissionAssignmentEntityService rolePermissionAssignmentEntityService;
    private final PagedQuerySupport pagedQuerySupport;
    private final PagedResponseFactory pagedResponseFactory;

    @PreAuthorize("hasAuthority('" + IDM_PERMISSION_READ + "')")
    public PagedResponseDTO<RoleDTO> handle(
            String permissionId,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        if (permissionId == null || permissionId.isBlank()) {
            throw new IllegalArgumentException("permissionId must not be blank");
        }

        pagedQuerySupport.validatePaging(page, size);

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = pagedQuerySupport.resolveSortDirection(sortDir);

        Page<RolePermissionAssignment> result = rolePermissionAssignmentEntityService.loadPageByPermissionId(
                permissionId,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        return pagedResponseFactory.fromPage(result, assignment -> {
            RoleDTO dto = new RoleDTO();
            dto.setId(assignment.getRole().getId());
            dto.setApplicationScopeId(assignment.getRole().getApplicationScope().getId());
            dto.setName(assignment.getRole().getName());
            dto.setDescription(assignment.getRole().getDescription());
            dto.setSystemProtected(assignment.getRole().isSystemProtected());
            return dto;
        });
    }

    private String resolveSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "role.name";
        }

        return switch (sortBy) {
            case "id" -> "role.id";
            case "name" -> "role.name";
            case "description" -> "role.description";
            case "systemProtected" -> "role.systemProtected";
            default -> throw new IllegalArgumentException("Unsupported sortBy for permission roles: " + sortBy);
        };
    }
}