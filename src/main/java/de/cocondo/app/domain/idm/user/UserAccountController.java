package de.cocondo.app.domain.idm.user;

import de.cocondo.app.domain.idm.user.dto.ChangePasswordRequestDTO;
import de.cocondo.app.domain.idm.user.dto.CreateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UpdateUserRequestDTO;
import de.cocondo.app.domain.idm.user.dto.UserAccountDTO;
import de.cocondo.app.system.dto.PagedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/list")
    public PagedResponseDTO<UserAccountDTO> listPaged(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sortBy", defaultValue = "username") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir,
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "displayName", required = false) String displayName,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "state", required = false) String state
    ) {
        return userAccountDomainService.listUsersPaged(
                page,
                size,
                sortBy,
                sortDir,
                username,
                displayName,
                email,
                state
        );
    }

    @GetMapping("/{id}")
    public UserAccountDTO getById(@PathVariable("id") String id) {
        return userAccountDomainService.getUserById(id);
    }

    @PutMapping("/{id}")
    public UserAccountDTO update(
            @PathVariable("id") String id,
            @RequestBody UpdateUserRequestDTO request
    ) {
        return userAccountDomainService.updateUser(id, request);
    }

    @PutMapping("/{id}/activate")
    public UserAccountDTO activate(@PathVariable("id") String id) {
        return userAccountDomainService.activate(id);
    }

    @PutMapping("/{id}/deactivate")
    public UserAccountDTO deactivate(@PathVariable("id") String id) {
        return userAccountDomainService.deactivate(id);
    }

    @PutMapping("/{id}/password")
    public UserAccountDTO changePassword(
            @PathVariable("id") String id,
            @RequestBody ChangePasswordRequestDTO request) {

        return userAccountDomainService.changePassword(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        userAccountDomainService.deleteUser(id);
    }
}