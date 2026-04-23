package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateUserHandler {

    private final UserAccountEntityService userAccountEntityService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final UserAccountDtoAssembler userAccountDtoAssembler;

    public UserAccountDTO handle(CreateUserRequestDTO request) {

        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("password must not be blank");
        }

        passwordPolicyValidator.validate(request.getPassword());

        Optional<UserAccount> existing =
                userAccountEntityService.loadByUsername(request.getUsername());

        if (existing.isPresent()) {
            throw new IllegalArgumentException("username already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(request.getUsername());
        user.setDisplayName(request.getDisplayName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.activate();

        UserAccount saved = userAccountEntityService.save(user);

        log.info("User created: id={}, username={}", saved.getId(), saved.getUsername());

        return userAccountDtoAssembler.toDto(saved);
    }
}