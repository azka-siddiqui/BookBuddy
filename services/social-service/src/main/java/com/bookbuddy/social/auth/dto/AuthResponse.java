package com.bookbuddy.social.auth.dto;

/**
 * Returned on successful registration or login.
 *
 * @param token       the signed JWT bearer token
 * @param expiresIn   token lifetime in seconds
 * @param userId      the authenticated user's id
 * @param username    the authenticated user's username
 * @param displayName the authenticated user's display name
 */
public record AuthResponse(
        String token,
        long expiresIn,
        String userId,
        String username,
        String displayName
) {
}
