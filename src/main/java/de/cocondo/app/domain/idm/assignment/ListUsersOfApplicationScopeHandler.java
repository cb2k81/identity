package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_SCOPE_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListUsersOfApplicationScopeHandler {

    private final UserApplicationScopeAssignmentEntityService userApplicationScopeAssignmentEntityService;

    @PreAuthorize("hasAuthority('" + IDM_SCOPE_READ + "')")
    public List<UserAccountDTO> handle(String applicationScopeId) {

        if (applicationScopeId == null || applicationScopeId.isBlank()) {
            throw new IllegalArgumentException("applicationScopeId must not be blank");
        }

        return userApplicationScopeAssignmentEntityService
                .loadAllByApplicationScopeId(applicationScopeId)
                .stream()
                .map(UserApplicationScopeAssignment::getUserAccount)
                .filter(user -> user != null)
                .map(user -> {
                    UserAccountDTO dto = new UserAccountDTO();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setDisplayName(user.getDisplayName());
                    dto.setEmail(user.getEmail());
                    dto.setState(user.getState());
                    return dto;
                })
                .toList();
    }
}