package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_PERMISSION_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListRolesOfPermissionPagedHandler {

    private final RolePermissionAssignmentEntityService rolePermissionAssignmentEntityService;

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

        validatePaging(page, size);

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = resolveSortDirection(sortDir);

        Page<RolePermissionAssignment> result = rolePermissionAssignmentEntityService.loadPageByPermissionId(
                permissionId,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        PagedResponseDTO<RoleDTO> response = new PagedResponseDTO<>();
        response.setItems(result.getContent().stream()
                .map(RolePermissionAssignment::getRole)
                .filter(role -> role != null)
                .map(role -> {
                    RoleDTO dto = new RoleDTO();
                    dto.setId(role.getId());
                    dto.setApplicationScopeId(role.getApplicationScope().getId());
                    dto.setName(role.getName());
                    dto.setDescription(role.getDescription());
                    dto.setSystemProtected(role.isSystemProtected());
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