package com.workshop.vehicle_service.auth.controller;

import com.workshop.vehicle_service.auth.dto.LoginRequest;
import com.workshop.vehicle_service.auth.dto.LoginResponse;
import com.workshop.vehicle_service.auth.dto.UserResponse;
import com.workshop.vehicle_service.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Authenticate user and return JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication success", content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication failed")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Get current authenticated profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile returned", content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid token")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(authService.currentUser(authentication));
    }

    @Operation(summary = "Manager-only protected endpoint")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authorized manager"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/manager/ping")
    public ResponseEntity<String> managerPing() {
        return ResponseEntity.ok("manager-ok");
    }
}
