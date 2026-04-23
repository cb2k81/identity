package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.role.dto.RoleDTO;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.system.dto.PagedResponseDTO;
import de.cocondo.app.system.list.PagedQuerySupport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListRolesOfUserInScopePagedHandler {

    private final ApplicationScopeEntityService applicationScopeEntityService;
    private final UserRoleAssignmentEntityService userRoleAssignmentEntityService;
    private final PagedQuerySupport pagedQuerySupport;

    @PreAuthorize("hasAuthority('" + IDM_USER_READ + "')")
    public PagedResponseDTO<RoleDTO> handle(
            String userAccountId,
            String applicationKey,
            String stageKey,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        if (userAccountId == null || userAccountId.isBlank()) {
            throw new IllegalArgumentException("userAccountId must not be blank");
        }
        if (applicationKey == null || applicationKey.isBlank()) {
            throw new IllegalArgumentException("applicationKey must not be blank");
        }
        if (stageKey == null || stageKey.isBlank()) {
            throw new IllegalArgumentException("stageKey must not be blank");
        }

        pagedQuerySupport.validatePaging(page, size);

        ApplicationScope scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ApplicationScope not found"));

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = pagedQuerySupport.resolveSortDirection(sortDir);

        Page<UserRoleAssignment> result = userRoleAssignmentEntityService.loadPageByUserAccountIdAndScope(
                userAccountId,
                applicationKey,
                stageKey,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        PagedResponseDTO<RoleDTO> response = new PagedResponseDTO<>();
        response.setItems(result.getContent().stream()
                .map(UserRoleAssignment::getRole)
                .filter(role -> role != null)
                .filter(role -> role.getApplicationScope() != null)
                .filter(role -> scope.getId().equals(role.getApplicationScope().getId()))
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

    private String resolveSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "role.name";
        }

        return switch (sortBy) {
            case "id" -> "role.id";
            case "name" -> "role.name";
            case "description" -> "role.description";
            case "systemProtected" -> "role.systemProtected";
            default -> throw new IllegalArgumentException("Unsupported sortBy for user roles in scope: " + sortBy);
        };
    }
}