package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.permission.dto.PermissionDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_ROLE_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListPermissionsOfRolePagedHandler {

    private final RolePermissionAssignmentEntityService rolePermissionAssignmentEntityService;

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

        validatePaging(page, size);

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = resolveSortDirection(sortDir);

        Page<RolePermissionAssignment> result = rolePermissionAssignmentEntityService.loadPageByRoleId(
                roleId,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        PagedResponseDTO<PermissionDTO> response = new PagedResponseDTO<>();
        response.setItems(result.getContent().stream()
                .map(RolePermissionAssignment::getPermission)
                .filter(permission -> permission != null)
                .map(permission -> {
                    PermissionDTO dto = new PermissionDTO();
                    dto.setId(permission.getId());
                    dto.setApplicationScopeId(permission.getApplicationScope().getId());
                    dto.setPermissionGroupId(permission.getPermissionGroup().getId());
                    dto.setName(permission.getName());
                    dto.setDescription(permission.getDescription());
                    dto.setSystemProtected(permission.isSystemProtected());
                    return dto;
                })
                .toList());
        response.setPage(result.getNumber());
        response.setSize(result.getSize());
        response.setTotalElements(result.getTotalElements());
        response.setTotalPages(result.getTotalPages());

        return response;
    }

    private void validatePaging(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        if (size > 200) {
            throw new IllegalArgumentException("size must be <= 200");
        }
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

    private Sort.Direction resolveSortDirection(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            return Sort.Direction.ASC;
        }

        return switch (sortDir.toLowerCase(Locale.ROOT)) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new IllegalArgumentException("Unsupported sortDir: " + sortDir);
        };
    }
}