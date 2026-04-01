package de.cocondo.app.domain.idm.scope;

import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_SCOPE_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListApplicationScopesPagedHandler {

    private final ApplicationScopeEntityService scopeEntityService;

    @PreAuthorize("hasAuthority('" + IDM_SCOPE_READ + "')")
    public PagedResponseDTO<ApplicationScopeDTO> handle(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String applicationKey,
            String stageKey,
            String description
    ) {

        validatePaging(page, size);

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = resolveSortDirection(sortDir);
        Specification<ApplicationScope> specification = buildSpecification(applicationKey, stageKey, description);

        Page<ApplicationScope> result = scopeEntityService.loadPage(
                specification,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        PagedResponseDTO<ApplicationScopeDTO> response = new PagedResponseDTO<>();
        response.setItems(result.getContent().stream().map(this::toDto).toList());
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
            return "applicationKey";
        }

        return switch (sortBy) {
            case "id", "applicationKey", "stageKey", "description" -> sortBy;
            default -> throw new IllegalArgumentException("Unsupported sortBy for scopes: " + sortBy);
        };
    }

    private Sort.Direction resolveSortDirection(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            return Sort.Direction.ASC;
        }

        if ("asc".equalsIgnoreCase(sortDir)) {
            return Sort.Direction.ASC;
        }
        if ("desc".equalsIgnoreCase(sortDir)) {
            return Sort.Direction.DESC;
        }

        throw new IllegalArgumentException("sortDir must be 'asc' or 'desc'");
    }

    private Specification<ApplicationScope> buildSpecification(
            String applicationKey,
            String stageKey,
            String description
    ) {
        Specification<ApplicationScope> specification = Specification.where(null);

        if (applicationKey != null && !applicationKey.isBlank()) {
            specification = specification.and(
                    (root, query, cb) -> cb.like(
                            cb.lower(root.get("applicationKey")),
                            "%" + applicationKey.trim().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        if (stageKey != null && !stageKey.isBlank()) {
            specification = specification.and(
                    (root, query, cb) -> cb.like(
                            cb.lower(root.get("stageKey")),
                            "%" + stageKey.trim().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        if (description != null && !description.isBlank()) {
            specification = specification.and(
                    (root, query, cb) -> cb.like(
                            cb.lower(root.get("description")),
                            "%" + description.trim().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        return specification;
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