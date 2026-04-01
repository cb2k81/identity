package de.cocondo.app.domain.idm.assignment;

import de.cocondo.app.domain.idm.scope.dto.ApplicationScopeDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static de.cocondo.app.config.IdmManagementAuthorities.IDM_USER_READ;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ListApplicationScopesOfUserHandler {

    private final UserApplicationScopeAssignmentEntityService userApplicationScopeAssignmentEntityService;

    @PreAuthorize("hasAuthority('" + IDM_USER_READ + "')")
    public List<ApplicationScopeDTO> handle(String userAccountId) {

        if (userAccountId == null || userAccountId.isBlank()) {
            throw new IllegalArgumentException("userAccountId must not be blank");
        }

        return userApplicationScopeAssignmentEntityService
                .loadAllByUserAccountId(userAccountId)
                .stream()
                .map(UserApplicationScopeAssignment::getApplicationScope)
                .filter(scope -> scope != null)
                .map(scope -> {
                    ApplicationScopeDTO dto = new ApplicationScopeDTO();
                    dto.setId(scope.getId());
                    dto.setApplicationKey(scope.getApplicationKey());
                    dto.setStageKey(scope.getStageKey());
                    dto.setDescription(scope.getDescription());
                    return dto;
                })
                .toList();
    }
}