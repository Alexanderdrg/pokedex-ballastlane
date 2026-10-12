package com.ballastlane.pokedex.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CredentialsRequest(
        @NotBlank @Size(max = 30) String username,
        @NotBlank @Size(max = 72) String password
) {
}
