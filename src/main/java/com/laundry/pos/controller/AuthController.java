package com.laundry.pos.controller;

import com.laundry.pos.request.AuthRequest;
import com.laundry.pos.response.AuthResponse;
import com.laundry.pos.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody AuthRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(
            @RequestHeader("Authorization") String authorizationHeader
    ) {

        String token = authorizationHeader.substring(7);

        return ResponseEntity.ok(
                authService.logout(token)
        );
    }
}