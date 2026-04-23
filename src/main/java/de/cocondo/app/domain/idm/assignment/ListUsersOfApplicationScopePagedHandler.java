package de.cocondo.app.domain.idm.assignment;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_SCOPE_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListUsersOfApplicationScopePagedHandler {

    private final UserApplicationScopeAssignmentEntityService userApplicationScopeAssignmentEntityService;
    private final UserAccountDtoAssembler userAccountDtoAssembler;
    private final PagedQuerySupport pagedQuerySupport;
    private final PagedResponseFactory pagedResponseFactory;

    @PreAuthorize("hasAuthority('" + IDM_SCOPE_READ + "')")
    public PagedResponseDTO<UserAccountDTO> handle(
            String applicationScopeId,
            int page,
            int size,
            String sortBy,
            String sortDir,
            String username,
            String displayName,
            String email,
            String state
    ) {
        if (applicationScopeId == null || applicationScopeId.isBlank()) {
            throw new IllegalArgumentException("applicationScopeId must not be blank");
        }

        pagedQuerySupport.validatePaging(page, size);

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = pagedQuerySupport.resolveSortDirection(sortDir);

        String normalizedUsername = normalizeFilter(username);
        String normalizedDisplayName = normalizeFilter(displayName);
        String normalizedEmail = normalizeFilter(email);
        UserAccountState parsedState = parseState(state);

        Page<UserApplicationScopeAssignment> result =
                userApplicationScopeAssignmentEntityService.loadPageByApplicationScopeId(
                        applicationScopeId,
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
            default -> throw new IllegalArgumentException("Unsupported sortBy for scope users: " + sortBy);
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
            throw new IllegalArgumentException("Unsupported state for scope users: " + state);
        }
    }
}