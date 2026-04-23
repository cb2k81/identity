package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import de.cocondo.app.system.list.PagedQuerySupport;
import de.cocondo.app.system.list.PagedResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ListUsersPagedHandler {

    private final UserAccountEntityService userAccountEntityService;
    private final UserAccountDtoAssembler userAccountDtoAssembler;
    private final PagedQuerySupport pagedQuerySupport;
    private final PagedResponseFactory pagedResponseFactory;

    public PagedResponseDTO<UserAccountDTO> handle(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String username,
            String displayName,
            String email,
            String state
    ) {

        pagedQuerySupport.validatePaging(page, size);

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = pagedQuerySupport.resolveSortDirection(sortDir);
        Specification<UserAccount> specification = buildSpecification(username, displayName, email, state);

        Page<UserAccount> result = userAccountEntityService.loadPage(
                specification,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        return pagedResponseFactory.fromPage(result, userAccountDtoAssembler::toDto);
    }

    private String resolveSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "username";
        }

        return switch (sortBy) {
            case "id", "username", "displayName", "email", "state" -> sortBy;
            default -> throw new IllegalArgumentException("Unsupported sortBy for users: " + sortBy);
        };
    }

    private Specification<UserAccount> buildSpecification(
            String username,
            String displayName,
            String email,
            String state
    ) {
        Specification<UserAccount> specification = Specification.where(null);

        if (username != null && !username.isBlank()) {
            specification = specification.and(
                    (root, query, cb) -> cb.like(
                            cb.lower(root.get("username")),
                            "%" + username.trim().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        if (displayName != null && !displayName.isBlank()) {
            specification = specification.and(
                    (root, query, cb) -> cb.like(
                            cb.lower(root.get("displayName")),
                            "%" + displayName.trim().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        if (email != null && !email.isBlank()) {
            specification = specification.and(
                    (root, query, cb) -> cb.like(
                            cb.lower(root.get("email")),
                            "%" + email.trim().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }

        if (state != null && !state.isBlank()) {
            UserAccountState parsedState;
            try {
                parsedState = UserAccountState.valueOf(state.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unsupported state for users: " + state);
            }

            specification = specification.and(
                    (root, query, cb) -> cb.equal(root.get("state"), parsedState)
            );
        }

        return specification;
    }
}