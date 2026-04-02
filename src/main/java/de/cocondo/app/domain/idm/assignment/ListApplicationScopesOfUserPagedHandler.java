package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.scope.ApplicationScope;
import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListApplicationScopesOfUserPagedHandler {

    private final UserApplicationScopeAssignmentEntityService userApplicationScopeAssignmentEntityService;

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

        validatePaging(page, size);

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = resolveSortDirection(sortDir);

        Page<UserApplicationScopeAssignment> result = userApplicationScopeAssignmentEntityService.loadPageByUserAccountId(
                userAccountId,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        PagedResponseDTO<ApplicationScopeDTO> response = new PagedResponseDTO<>();
        response.setItems(result.getContent().stream()
                .map(UserApplicationScopeAssignment::getApplicationScope)
                .filter(scope -> scope != null)
                .map(this::toDto)
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

    private ApplicationScopeDTO toDto(ApplicationScope scope) {
        ApplicationScopeDTO dto = new ApplicationScopeDTO();
        dto.setId(scope.getId());
        dto.setApplicationKey(scope.getApplicationKey());
        dto.setStageKey(scope.getStageKey());
        dto.setDescription(scope.getDescription());
        return dto;
    }
}