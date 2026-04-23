package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.user.dto.UpdateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateUserHandler {

    private final UserAccountEntityService userAccountEntityService;
    private final UserAccountDtoAssembler userAccountDtoAssembler;

    public UserAccountDTO handle(String id, UpdateUserRequestDTO request) {

        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        UserAccount user = loadUserByIdRequired(id);

        user.setDisplayName(request.getDisplayName());
        user.setEmail(request.getEmail());

        UserAccount saved = userAccountEntityService.save(user);

        log.info("User updated: id={}, username={}", saved.getId(), saved.getUsername());

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