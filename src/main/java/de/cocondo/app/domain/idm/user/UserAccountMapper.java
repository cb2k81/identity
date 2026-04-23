package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Central mapper for UserAccount -> UserAccountDTO base fields.
 *
 * Responsibilities:
 * - map direct entity-backed fields only
 * - no session aggregation logic
 * - no controller/service orchestration
 */
@Mapper(componentModel = "spring")
public interface UserAccountMapper {

    @Mapping(target = "loginCount", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    UserAccountDTO toDto(UserAccount userAccount);
}