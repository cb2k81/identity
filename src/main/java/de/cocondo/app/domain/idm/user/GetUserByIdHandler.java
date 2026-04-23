package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserByIdHandler {

    private final UserAccountEntityService userAccountEntityService;
    private final UserAccountDtoAssembler userAccountDtoAssembler;

    public UserAccountDTO handle(String id) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }

        UserAccount user = userAccountEntityService.loadById(id)
                .orElseThrow(() -> new EntityNotFoundException("UserAccount not found: id=" + id));

        return userAccountDtoAssembler.toDto(user);
    }
}