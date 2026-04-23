package de.cocondo.app.domain.idm.user;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteUserHandler {

    private final UserAccountEntityService userAccountEntityService;

    public void handle(String id) {

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
}