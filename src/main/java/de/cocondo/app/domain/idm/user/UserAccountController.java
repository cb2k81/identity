package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.user.dto.ChangePasswordRequestDTO;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for managing UserAccount aggregates.
 *
 * Authorization is enforced at domain service level via @PreAuthorize.
 */
@RestController
@RequestMapping("/api/idm/users")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountDomainService userAccountDomainService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserAccountDTO create(@RequestBody CreateUserRequestDTO request) {
        return userAccountDomainService.createUser(request);
    }

    @GetMapping("/{id}")
    public UserAccountDTO getById(@PathVariable String id) {
        return userAccountDomainService.getUserById(id);
    }

    @GetMapping
    public List<UserAccountDTO> list() {
        return userAccountDomainService.listUsers();
    }

    @PutMapping("/{id}/activate")
    public UserAccountDTO activate(@PathVariable String id) {
        return userAccountDomainService.activate(id);
    }

    @PutMapping("/{id}/deactivate")
    public UserAccountDTO deactivate(@PathVariable String id) {
        return userAccountDomainService.deactivate(id);
    }

    @PutMapping("/{id}/password")
    public UserAccountDTO changePassword(@PathVariable String id, @RequestBody ChangePasswordRequestDTO request) {
        return userAccountDomainService.changePassword(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        userAccountDomainService.deleteUser(id);
    }
}