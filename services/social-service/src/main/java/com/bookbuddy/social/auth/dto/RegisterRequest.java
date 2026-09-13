package com.bookbuddy.social.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 30) String username,
        @Size(max = 60) String displayName,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}
