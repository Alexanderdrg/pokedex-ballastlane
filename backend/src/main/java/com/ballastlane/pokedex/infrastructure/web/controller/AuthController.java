package com.ballastlane.pokedex.infrastructure.web.controller;

import com.ballastlane.pokedex.application.usecase.AuthUseCase;
import com.ballastlane.pokedex.infrastructure.security.AuthenticatedUser;
import com.ballastlane.pokedex.infrastructure.web.dto.AuthResponse;
import com.ballastlane.pokedex.infrastructure.web.dto.CredentialsRequest;
import com.ballastlane.pokedex.infrastructure.web.dto.CurrentUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registration and authentication")
public class AuthController {

    private final AuthUseCase auth;

    public AuthController(AuthUseCase auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user (role USER) and get a token")
    public AuthResponse register(@Valid @RequestBody CredentialsRequest request) {
        return AuthResponse.from(auth.register(request.username(), request.password()));
    }

    @PostMapping("/login")
    @Operation(summary = "Exchange credentials for a JWT")
    public AuthResponse login(@Valid @RequestBody CredentialsRequest request) {
        return AuthResponse.from(auth.login(request.username(), request.password()));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "Current authenticated user")
    public CurrentUserResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return new CurrentUserResponse(user.username(), user.role().name());
    }
}
