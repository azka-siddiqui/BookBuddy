package com.bookbuddy.social.auth;

import com.bookbuddy.social.config.JwtProperties;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    // 256-bit base64 secret for tests.
    private static final String SECRET = "dGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtdGVzdC1zZWNyZXQtMzI=";

    private JwtService service(String secret) {
        return new JwtService(new JwtProperties(secret, 3_600_000L));
    }

    @Test
    void issuesAndParsesTokenRoundTrip() {
        JwtService jwt = service(SECRET);

        String token = jwt.issueToken("user-1", "alex", "Alex", List.of("USER"));
        AuthenticatedUser user = jwt.parse(token);

        assertThat(user.userId()).isEqualTo("user-1");
        assertThat(user.username()).isEqualTo("alex");
        assertThat(user.displayName()).isEqualTo("Alex");
        assertThat(user.roles()).containsExactly("USER");
    }

    @Test
    void rejectsTokenSignedWithDifferentKey() {
        String otherSecret = "b3RoZXItc2VjcmV0LW90aGVyLXNlY3JldC1vdGhlci1zZWNyZXQtMzI=";
        String token = service(SECRET).issueToken("user-1", "alex", "Alex", List.of("USER"));

        JwtService differentKeyService = service(otherSecret);

        assertThatThrownBy(() -> differentKeyService.parse(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsMalformedToken() {
        assertThatThrownBy(() -> service(SECRET).parse("not-a-jwt"))
                .isInstanceOf(JwtException.class);
    }
}
