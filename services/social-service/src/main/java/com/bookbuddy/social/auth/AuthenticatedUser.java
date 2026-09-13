package com.bookbuddy.social.auth;

import java.util.List;

/**
 * The authenticated principal carried in the security context, derived from a
 * validated JWT.
 */
public record AuthenticatedUser(
        String userId,
        String username,
        String displayName,
        List<String> roles
) {
}
