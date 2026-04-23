package de.cocondo.app.domain.idm.scope;

import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import de.cocondo.app.system.list.PagedQuerySupport;
import de.cocondo.app.system.list.PagedResponseFactory;
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
    private final PagedQuerySupport pagedQuerySupport;
    private final PagedResponseFactory pagedResponseFactory;

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

        pagedQuerySupport.validatePaging(page, size);

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = pagedQuerySupport.resolveSortDirection(sortDir);
        Specification<ApplicationScope> specification = buildSpecification(applicationKey, stageKey, description);

        Page<ApplicationScope> result = scopeEntityService.loadPage(
                specification,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        return pagedResponseFactory.fromPage(result, this::toDto);
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