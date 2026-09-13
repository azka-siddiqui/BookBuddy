package com.bookbuddy.social.auth;

import com.bookbuddy.social.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

/**
 * Issues and validates JSON Web Tokens (HS256).
 *
 * <p>The token subject is the user id; the username, display name and roles are
 * carried as custom claims so downstream services can identify the caller without
 * a database round-trip.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(JwtProperties properties) {
        byte[] keyBytes = Decoders.BASE64.decode(properties.secret());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = properties.expirationMs();
    }

    /**
     * Issues a signed token for the given user.
     *
     * @param userId      subject of the token
     * @param username    username claim
     * @param displayName display-name claim
     * @param roles       roles claim
     * @return the compact, signed JWT
     */
    public String issueToken(String userId, String username, String displayName, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("displayName", displayName)
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parses and validates a token.
     *
     * @param token the compact JWT
     * @return the authenticated principal derived from the token claims
     * @throws JwtException if the token is malformed, expired or has an invalid signature
     */
    public AuthenticatedUser parse(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);

        Claims claims = jws.getPayload();
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        return new AuthenticatedUser(
                claims.getSubject(),
                claims.get("username", String.class),
                claims.get("displayName", String.class),
                roles == null ? List.of() : roles);
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
