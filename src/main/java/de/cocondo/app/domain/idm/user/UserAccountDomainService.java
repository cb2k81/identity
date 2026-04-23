package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.user.dto.ChangePasswordRequestDTO;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UpdateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_CREATE;
import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_DELETE;
import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_READ;
import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_UPDATE;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAccountDomainService {

    private final CreateUserHandler createUserHandler;
    private final GetUserByIdHandler getUserByIdHandler;
    private final ListUsersPagedHandler listUsersPagedHandler;
    private final UpdateUserHandler updateUserHandler;
    private final ActivateUserHandler activateUserHandler;
    private final DeactivateUserHandler deactivateUserHandler;
    private final ChangeUserPasswordHandler changeUserPasswordHandler;
    private final DeleteUserHandler deleteUserHandler;

    @PreAuthorize("hasAuthority('" + IDM_USER_CREATE + "')")
    public UserAccountDTO createUser(CreateUserRequestDTO request) {
        return createUserHandler.handle(request);
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_READ + "')")
    @Transactional(readOnly = true)
    public UserAccountDTO getUserById(String id) {
        return getUserByIdHandler.handle(id);
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
            String state,
            Integer failedLoginAttempts,
            String lockedUntil,
            String lastModifiedAt
    ) {
        return listUsersPagedHandler.handle(
                page,
                size,
                sortBy,
                sortDir,
                username,
                displayName,
                email,
                state,
                failedLoginAttempts,
                lockedUntil,
                lastModifiedAt
        );
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_UPDATE + "')")
    public UserAccountDTO updateUser(String id, UpdateUserRequestDTO request) {
        return updateUserHandler.handle(id, request);
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_UPDATE + "')")
    public UserAccountDTO activate(String id) {
        return activateUserHandler.handle(id);
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_UPDATE + "')")
    public UserAccountDTO deactivate(String id) {
        return deactivateUserHandler.handle(id);
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_UPDATE + "')")
    public UserAccountDTO changePassword(String id, ChangePasswordRequestDTO request) {
        return changeUserPasswordHandler.handle(id, request);
    }

    @PreAuthorize("hasAuthority('" + IDM_USER_DELETE + "')")
    public void deleteUser(String id) {
        deleteUserHandler.handle(id);
    }
}