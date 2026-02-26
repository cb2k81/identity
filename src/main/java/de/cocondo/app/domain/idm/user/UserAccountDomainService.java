package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.user.dto.ChangePasswordRequestDTO;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static de.cocondo.app.domain.idm.management.IdmManagementAuthorities.IDM_USER_CREATE;
import static de.cocondo.app.domain.idm.management.IdmManagementAuthorities.IDM_USER_DELETE;
import static de.cocondo.app.domain.idm.management.IdmManagementAuthorities.IDM_USER_READ;
import static de.cocondo.app.domain.idm.management.IdmManagementAuthorities.IDM_USER_UPDATE;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserAccountDomainService {

    private final UserAccountEntityService userAccountEntityService;
    private final PasswordEncoder passwordEncoder;

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

        Optional<UserAccount> existing =
                userAccountEntityService.loadByUsername(request.getUsername());

        if (existing.isPresent()) {
            throw new IllegalArgumentException("username already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
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

    private UserAccountDTO mapToDto(UserAccount user) {

        UserAccountDTO dto = new UserAccountDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setState(user.getState());
        return dto;
    }
}