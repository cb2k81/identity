package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.role.Role;
import de.cocondo.app.domain.idm.role.RoleEntityService;
import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.ApplicationScopeEntityService;
import de.cocondo.app.domain.idm.user.UserAccountDtoAssembler;
import de.cocondo.app.domain.idm.user.UserAccountState;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import de.cocondo.app.system.list.PagedQuerySupport;
import de.cocondo.app.system.list.PagedResponseFactory;
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
    private final UserAccountDtoAssembler userAccountDtoAssembler;
    private final PagedQuerySupport pagedQuerySupport;
    private final PagedResponseFactory pagedResponseFactory;

    @PreAuthorize("hasAuthority('" + IDM_ROLE_READ + "')")
    public PagedResponseDTO<UserAccountDTO> handle(
            String roleId,
            String applicationKey,
            String stageKey,
            int page,
            int size,
            String sortBy,
            String sortDir,
            String username,
            String displayName,
            String email,
            String state
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

        pagedQuerySupport.validatePaging(page, size);

        ApplicationScope scope = applicationScopeEntityService
                .loadByApplicationKeyAndStageKey(applicationKey, stageKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ApplicationScope not found"));

        Role role = roleEntityService.loadById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

        if (role.getApplicationScope() == null || !scope.getId().equals(role.getApplicationScope().getId())) {
            throw new IllegalArgumentException("Role does not belong to requested scope");
        }

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = pagedQuerySupport.resolveSortDirection(sortDir);

        String normalizedUsername = normalizeFilter(username);
        String normalizedDisplayName = normalizeFilter(displayName);
        String normalizedEmail = normalizeFilter(email);
        UserAccountState parsedState = parseState(state);

        Page<UserRoleAssignment> result = userRoleAssignmentEntityService.loadPageByRoleIdAndScope(
                roleId,
                applicationKey,
                stageKey,
                normalizedUsername,
                normalizedDisplayName,
                normalizedEmail,
                parsedState,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        return pagedResponseFactory.fromPage(
                result,
                assignment -> userAccountDtoAssembler.toDto(assignment.getUserAccount())
        );
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

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private UserAccountState parseState(String state) {
        if (state == null || state.isBlank()) {
            return null;
        }

        try {
            return UserAccountState.valueOf(state.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported state for role users in scope: " + state);
        }
    }
}