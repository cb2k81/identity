// Datei: src/main/java/de/cocondo/app/domain/idm/user/UserAccountDomainService.java
package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.auth.session.AuthSessionEntityService;
import de.cocondo.app.domain.idm.user.dto.ChangePasswordRequestDTO;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UpdateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_CREATE;
import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_DELETE;
import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_READ;
import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_UPDATE;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserAccountDomainService {

    private final UserAccountEntityService userAccountEntityService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final AuthSessionEntityService authSessionEntityService;

    @PreAuthorize("hasAuthority('" + IDM_USER_CREATE + "')")
    public UserAccountDTO createUser(CreateUserRequestDTO request) {

        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("password must not be blank");
        }

        passwordPolicyValidator.validate(request.getPassword());

        Optional<UserAccount> existing =
                userAccountEntityService.loadByUsername(request.getUsername());

        if (existing.isPresent()) {
            throw new IllegalArgumentException("username already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setDisplayName(request.getDisplayName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.activate();

        UserAccount saved = userAccountEntityService.save(user);

        log.info("User created: id={}, username={}", saved.getId(), saved.getUsername());

        return mapToDto(saved);
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_READ + "')")
    @Transactional(readOnly = true)
    public UserAccountDTO getUserById(String id) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }

        UserAccount user = userAccountEntityService.loadById(id)
                .orElseThrow(() -> new EntityNotFoundException("UserAccount not found: id=" + id));

        return mapToDto(user);
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_READ + "')")
    @Transactional(readOnly = true)
    public List<UserAccountDTO> listUsers() {

        return userAccountEntityService.loadAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_READ + "')")
    @Transactional(readOnly = true)
    public PagedResponseDTO<UserAccountDTO> listUsersPaged(
            int page,
            int size,
            String sortBy,
            String sortDir,
            String username,
            String displayName,
            String email,
            String state
    ) {

        validatePaging(page, size);

        String resolvedSortBy = resolveSortBy(sortBy);
        Sort.Direction direction = resolveSortDirection(sortDir);
        Specification<UserAccount> specification = buildSpecification(username, displayName, email, state);

        Page<UserAccount> result = userAccountEntityService.loadPage(
                specification,
                PageRequest.of(page, size, Sort.by(direction, resolvedSortBy))
        );

        PagedResponseDTO<UserAccountDTO> response = new PagedResponseDTO<>();
        response.setItems(result.getContent().stream().map(this::mapToDto).toList());
        response.setPage(result.getNumber());
        response.setSize(result.getSize());
        response.setTotalElements(result.getTotalElements());
        response.setTotalPages(result.getTotalPages());

        return response;
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_UPDATE + "')")
    public UserAccountDTO updateUser(String id, UpdateUserRequestDTO request) {

        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        UserAccount user = loadUserByIdRequired(id);

        user.setDisplayName(request.getDisplayName());
        user.setEmail(request.getEmail());

        UserAccount saved = userAccountEntityService.save(user);

        log.info("User updated: id={}, username={}", saved.getId(), saved.getUsername());

        return mapToDto(saved);
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_UPDATE + "')")
    public UserAccountDTO activate(String id) {

        UserAccount user = loadUserByIdRequired(id);

        user.activate();

        UserAccount saved = userAccountEntityService.save(user);

        return mapToDto(saved);
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_UPDATE + "')")
    public UserAccountDTO deactivate(String id) {

        UserAccount user = loadUserByIdRequired(id);

        user.disable();

        UserAccount saved = userAccountEntityService.save(user);

        return mapToDto(saved);
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_UPDATE + "')")
    public UserAccountDTO changePassword(String id, ChangePasswordRequestDTO request) {

        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("newPassword must not be blank");
        }

        passwordPolicyValidator.validate(request.getNewPassword());

        UserAccount user = loadUserByIdRequired(id);

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        UserAccount saved = userAccountEntityService.save(user);

        return mapToDto(saved);
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_DELETE + "')")
    public void deleteUser(String id) {

        UserAccount user = loadUserByIdRequired(id);

        userAccountEntityService.delete(user);
    }

    private UserAccount loadUserByIdRequired(String id) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }

        return userAccountEntityService.loadById(id)
                .orElseThrow(() -> new EntityNotFoundException("UserAccount not found: id=" + id));
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
            return "username";
        }

        return switch (sortBy) {
            case "id", "username", "displayName", "email", "state" -> sortBy;
            default -> throw new IllegalArgumentException("Unsupported sortBy for users: " + sortBy);
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

    private UserAccountDTO mapToDto(UserAccount user) {

        UserAccountDTO dto = new UserAccountDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setDisplayName(user.getDisplayName());
        dto.setEmail(user.getEmail());
        dto.setState(user.getState());
        dto.setFailedLoginAttempts(user.getFailedLoginAttempts());
        dto.setLockedUntil(user.getLockedUntil());
        dto.setCreatedBy(user.getCreatedBy());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setLastModifiedBy(user.getLastModifiedBy());
        dto.setLastModifiedAt(user.getLastModifiedAt());
        dto.setLoginCount(authSessionEntityService.countByUserAccountId(user.getId()));
        dto.setLastLogin(authSessionEntityService.findLastLoginAtByUserAccountId(user.getId()).orElse(null));
        return dto;
    }
}