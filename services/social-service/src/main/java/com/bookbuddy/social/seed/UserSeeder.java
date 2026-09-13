package com.bookbuddy.social.seed;

import com.bookbuddy.social.user.User;
import com.bookbuddy.social.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * Loads the demo users from a seed file on startup (dev profile only), hashing
 * each plaintext password with the application's {@link PasswordEncoder} before
 * persisting. This guarantees the demo credentials work through the real login
 * flow while keeping plaintext passwords out of the database.
 *
 * <p>The seed file location is configurable so it can be mounted into a container;
 * it defaults to {@code classpath:seed/users.json}.
 */
@Component
@Profile("dev")
public class UserSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(UserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final String seedLocation;

    public UserSeeder(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      ResourceLoader resourceLoader,
                      ObjectMapper objectMapper,
                      org.springframework.core.env.Environment env) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
        this.seedLocation = env.getProperty("bookbuddy.seed.users-location", "classpath:seed/users.json");
    }

    @Override
    public void run(String... args) throws Exception {
        Resource resource = resourceLoader.getResource(seedLocation);
        if (!resource.exists()) {
            log.info("No user seed file at '{}'; skipping user seeding.", seedLocation);
            return;
        }

        List<SeedUser> seedUsers;
        try (InputStream in = resource.getInputStream()) {
            seedUsers = objectMapper.readValue(in, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, SeedUser.class));
        }

        int created = 0;
        for (SeedUser seed : seedUsers) {
            if (userRepository.existsByUsername(seed.username())) {
                continue;
            }
            User user = new User();
            user.setId(seed._id());
            user.setUsername(seed.username());
            user.setDisplayName(seed.displayName());
            user.setPasswordHash(passwordEncoder.encode(seed.password()));
            user.setPersona(seed.persona());
            user.setRoles(seed.roles());
            user.setStreakCount(seed.streakCount());
            user.setLastReadDate(seed.lastReadDate());
            user.setCreatedAt(seed.createdAt());
            userRepository.save(user);
            created++;
        }

        log.info("User seeding complete: {} new user(s) created from '{}'.", created, seedLocation);
    }
}
