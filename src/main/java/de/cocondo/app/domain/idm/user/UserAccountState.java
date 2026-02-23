package de.cocondo.app.domain.idm.user;

/**
 * Workflow state of a UserAccount.
 *
 * This state models the lifecycle and operational status of a user account.
 * It is owned by the UserAccount aggregate and must not be duplicated in
 * separate assignment entities.
 */
public enum UserAccountState {

    ACTIVE,

    EXPIRED,

    LOCKED_TEMPORARY,

    LOCKED_PERMANENT,

    DISABLED
}