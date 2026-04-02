package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.role.RoleEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_ROLE_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListUsersOfRoleInScopePagedHandler {

    private final ApplicationScopeEntityService applicationScopeEntityService;
    private final RoleEntityService roleEntityService;
    private final UserRoleAssignmentEntityService userRoleAssignmentEntityService;

    @PreAuthorize("hasAuthority('" + IDM_ROLE_READ + "')")
    public PagedResponseDTO<UserAccountDTO> handle(
            String roleId,
            String applicationKey,
            String stageKey,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        if (roleId == null || roleId.isBlank()) {
            throw new IllegalArgumentException("roleId must not be blank");
        }
        if (applicationKey == null || applicationKey.isBlank()) {
            throw new IllegalArgumentException("applicationKey must not be blank");
        }
        if (stageKey == null || stageKey.isBlank()) {
            throw new IllegalArgumentException("stageKey must not be blank");
        }

        validatePaging(page, size);

        ApplicationScope scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ApplicationScope not found"));

        Role role = roleEntityService.loadById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        if (role.getApplicationScope() == null || !scope.getId().equals(role.getApplicationScope().getId())) {
            throw new IllegalArgumentException("Role does not belong to requested scope");
        }

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = resolveSortDirection(sortDir);

        Page<UserRoleAssignment> result = userRoleAssignmentEntityService.loadPageByRoleIdAndScope(
                roleId,
                applicationKey,
                stageKey,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        PagedResponseDTO<UserAccountDTO> response = new PagedResponseDTO<>();
        response.setItems(result.getContent().stream()
                .map(UserRoleAssignment::getUserAccount)
                .filter(user -> user != null)
                .map(user -> {
                    UserAccountDTO dto = new UserAccountDTO();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setDisplayName(user.getDisplayName());
                    dto.setEmail(user.getEmail());
                    dto.setState(user.getState());
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
            return "userAccount.username";
        }

        return switch (sortBy) {
            case "id" -> "userAccount.id";
            case "username" -> "userAccount.username";
            case "displayName" -> "userAccount.displayName";
            case "email" -> "userAccount.email";
            case "state" -> "userAccount.state";
            default -> throw new IllegalArgumentException("Unsupported sortBy for role users in scope: " + sortBy);
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