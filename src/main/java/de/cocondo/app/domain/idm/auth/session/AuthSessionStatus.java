package de.cocondo.app.domain.idm.auth.session;

/**
 * Workflow status of a persisted AuthSession.
 */
public enum AuthSessionStatus {

    ACTIVE,

    REVOKED,

    EXPIRED
}