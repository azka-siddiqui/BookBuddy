package com.bookbuddy.gateway.security;

import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Central authentication for the platform.
 *
 * <p>For every non-public request the filter requires a valid bearer token; the
 * validated user id is injected as the {@code X-User-Id} header that downstream
 * services trust. Any client-supplied {@code X-User-Id} is stripped first so it
 * cannot be spoofed. Public paths (registration, login) are forwarded untouched.
 */
@Component
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationGlobalFilter.class);

    private static final String USER_HEADER = "X-User-Id";
    private static final String BEARER_PREFIX = "Bearer ";

    /** Paths that do not require authentication. */
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login");

    private final JwtValidator jwtValidator;

    public AuthenticationGlobalFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        if (isPublic(path)) {
            // Still strip any spoofed identity header on public routes.
            return chain.filter(withoutUserHeader(exchange));
        }

        String token = extractBearerToken(request);
        if (token == null) {
            return unauthorized(exchange, "Missing bearer token");
        }

        try {
            String userId = jwtValidator.extractUserId(token);
            ServerWebExchange mutated = exchange.mutate()
                    .request(r -> r.headers(headers -> {
                        headers.remove(USER_HEADER);
                        headers.add(USER_HEADER, userId);
                    }))
                    .build();
            return chain.filter(mutated);
        } catch (JwtException ex) {
            log.debug("Rejected request to {}: {}", path, ex.getMessage());
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private String extractBearerToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    private ServerWebExchange withoutUserHeader(ServerWebExchange exchange) {
        return exchange.mutate()
                .request(r -> r.headers(headers -> headers.remove(USER_HEADER)))
                .build();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        log.debug("401 Unauthorized: {}", reason);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        // Run before the routing filter so downstream services see X-User-Id.
        return -1;
    }
}
