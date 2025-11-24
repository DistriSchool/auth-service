package edu.unifor.br.distrischool.authservice.controller;

import edu.unifor.br.distrischool.authservice.dto.*;
import edu.unifor.br.distrischool.authservice.dto.UserProfileResponse;
import edu.unifor.br.distrischool.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    @Autowired
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<UserAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        UserAuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        MessageResponse response = authService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-email-verification")
    public ResponseEntity<MessageResponse> resendEmailVerification() {
        MessageResponse response = authService.resendEmailVerification();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<MessageResponse> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequest request
    ) {
        MessageResponse response = authService.requestPasswordReset(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        MessageResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<UserAuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        UserAuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<StatusResponse> health() {
        StatusResponse response = StatusResponse.builder()
            .status(SystemStatus.OK)
            .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me() {
        UserProfileResponse profile = authService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }
}
