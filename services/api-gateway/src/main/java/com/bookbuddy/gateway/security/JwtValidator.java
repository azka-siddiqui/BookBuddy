package com.bookbuddy.gateway.security;

import com.bookbuddy.gateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Validates JWTs at the edge and extracts the caller's user id. The gateway only
 * needs the subject (user id); full claim handling lives in social-service.
 */
@Component
public class JwtValidator {

    private final SecretKey signingKey;

    public JwtValidator(JwtProperties properties) {
        byte[] keyBytes = Decoders.BASE64.decode(properties.secret());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Validates the token's signature and expiry and returns the subject (user id).
     *
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public String extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
}
