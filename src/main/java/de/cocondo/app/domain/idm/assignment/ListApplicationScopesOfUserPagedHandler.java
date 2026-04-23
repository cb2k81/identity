package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
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

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListApplicationScopesOfUserPagedHandler {

    private final UserApplicationScopeAssignmentEntityService userApplicationScopeAssignmentEntityService;
    private final PagedQuerySupport pagedQuerySupport;
    private final PagedResponseFactory pagedResponseFactory;

    @PreAuthorize("hasAuthority('" + IDM_USER_READ + "')")
    public PagedResponseDTO<ApplicationScopeDTO> handle(
            String userAccountId,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        if (userAccountId == null || userAccountId.isBlank()) {
            throw new IllegalArgumentException("userAccountId must not be blank");
        }

        pagedQuerySupport.validatePaging(page, size);

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = pagedQuerySupport.resolveSortDirection(sortDir);

        Page<UserApplicationScopeAssignment> result = userApplicationScopeAssignmentEntityService.loadPageByUserAccountId(
                userAccountId,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        return pagedResponseFactory.fromPage(result, assignment -> toDto(assignment.getApplicationScope()));
    }

    private String resolveSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "applicationScope.applicationKey";
        }

        return switch (sortBy) {
            case "id" -> "applicationScope.id";
            case "applicationKey" -> "applicationScope.applicationKey";
            case "stageKey" -> "applicationScope.stageKey";
            case "description" -> "applicationScope.description";
            default -> throw new IllegalArgumentException("Unsupported sortBy for user scopes: " + sortBy);
        };
    }

    private ApplicationScopeDTO toDto(ApplicationScope scope) {
        ApplicationScopeDTO dto = new ApplicationScopeDTO();
        dto.setId(scope.getId());
        dto.setApplicationKey(scope.getApplicationKey());
        dto.setStageKey(scope.getStageKey());
        dto.setDescription(scope.getDescription());
        return dto;
    }
}