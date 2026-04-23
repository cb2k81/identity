package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.auth.session.AuthSessionEntityService;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Central assembler for fully populated UserAccountDTO instances.
 *
 * Responsibilities:
 * - delegate base entity mapping to UserAccountMapper
 * - enrich DTO with login/session derived metrics
 *
 * Important:
 * - Keeps loginCount / lastLogin enrichment in exactly one place
 * - Allows all read paths to return the same DTO contract
 */
@Component
@RequiredArgsConstructor
public class UserAccountDtoAssembler {

    private final UserAccountMapper userAccountMapper;
    private final AuthSessionEntityService authSessionEntityService;

    public UserAccountDTO toDto(UserAccount userAccount) {

        UserAccountDTO dto = userAccountMapper.toDto(userAccount);

        if (dto == null || userAccount == null) {
            return dto;
        }

        dto.setLoginCount(authSessionEntityService.countByUserAccountId(userAccount.getId()));
        dto.setLastLogin(authSessionEntityService.findLastLoginAtByUserAccountId(userAccount.getId()).orElse(null));

        return dto;
    }
}