package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeactivateUserHandler {

    private final UserAccountEntityService userAccountEntityService;
    private final UserAccountDtoAssembler userAccountDtoAssembler;

    public UserAccountDTO handle(String id) {

        UserAccount user = loadUserByIdRequired(id);

        user.disable();

        UserAccount saved = userAccountEntityService.save(user);

        return userAccountDtoAssembler.toDto(saved);
    }

    private UserAccount loadUserByIdRequired(String id) {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }

        return userAccountEntityService.loadById(id)
                .orElseThrow(() -> new EntityNotFoundException("UserAccount not found: id=" + id));
    }
}