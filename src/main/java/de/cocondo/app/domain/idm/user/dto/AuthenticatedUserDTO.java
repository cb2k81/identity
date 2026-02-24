package de.cocondo.app.domain.idm.user.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO returned after successful authentication.
 *
 * Contains only the identity data needed by downstream services (e.g. token issuing).
 */
@Data
public class AuthenticatedUserDTO {

    private String id;

    private String username;

    /**
     * Application key of the authenticated scope.
     * Example: "IDM", "CRM".
     */
    private String applicationKey;

    /**
     * Stage key of the authenticated scope.
     * Example: "DEV", "TEST", "PROD".
     */
    private String stageKey;

    /**
     * Role names assigned to the user within the authenticated scope.
     *
     * NOTE:
     * - This list is scope-specific (applicationKey + stageKey).
     * - The list contains role names (not role IDs) to keep the token stable and portable.
     */
    private List<String> roles = new ArrayList<>();
}