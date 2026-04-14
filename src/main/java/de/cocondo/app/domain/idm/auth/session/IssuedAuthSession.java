package de.cocondo.app.domain.idm.auth.session;

/**
 * Internal value object returned when a new AuthSession is created.
 *
 * Contains only the values needed by higher layers to expose the refresh context
 * without leaking persistence details.
 *
 * @param sessionId         technical persisted AuthSession identifier
 * @param refreshToken      raw refresh token for the client
 * @param refreshExpiresAt  absolute expiration timestamp (epoch millis)
 */
public record IssuedAuthSession(
        String sessionId,
        String refreshToken,
        long refreshExpiresAt
) {
}