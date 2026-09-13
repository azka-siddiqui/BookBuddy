package com.bookbuddy.social.auth;

import com.bookbuddy.social.auth.dto.AuthResponse;
import com.bookbuddy.social.auth.dto.LoginRequest;
import com.bookbuddy.social.auth.dto.RegisterRequest;
import com.bookbuddy.social.user.User;
import com.bookbuddy.social.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Handles registration and login, issuing a JWT on success. Passwords are stored
 * only as BCrypt hashes.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user and returns a token so the client is logged in
     * immediately.
     *
     * @throws UsernameTakenException if the username already exists
     */
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim().toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new UsernameTakenException(username);
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setDisplayName(request.displayName() == null || request.displayName().isBlank()
                ? request.username().trim()
                : request.displayName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPersona("Casual Reader");
        user.setRoles(List.of("USER"));
        user.setStreakCount(0);
        user.setCreatedAt(Instant.now());
        userRepository.save(user);

        return toAuthResponse(user);
    }

    /**
     * Authenticates a user by username/password.
     *
     * @throws InvalidCredentialsException if the username is unknown or the password is wrong
     */
    public AuthResponse login(LoginRequest request) {
        String username = request.username().trim().toLowerCase();
        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.issueToken(
                user.getId(), user.getUsername(), user.getDisplayName(), user.getRoles());
        return new AuthResponse(
                token,
                jwtService.getExpirationMs() / 1000,
                user.getId(),
                user.getUsername(),
                user.getDisplayName());
    }
}
