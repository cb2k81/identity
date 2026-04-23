package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.user.dto.ChangePasswordRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeUserPasswordHandler {

    private final UserAccountEntityService userAccountEntityService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final UserAccountDtoAssembler userAccountDtoAssembler;

    public UserAccountDTO handle(String id, ChangePasswordRequestDTO request) {

        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("newPassword must not be blank");
        }

        passwordPolicyValidator.validate(request.getNewPassword());

        UserAccount user = loadUserByIdRequired(id);

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

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