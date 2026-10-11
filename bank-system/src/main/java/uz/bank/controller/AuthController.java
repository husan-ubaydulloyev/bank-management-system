package uz.bank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import uz.bank.dto.AuthDtos;
import uz.bank.dto.AuthDtos.AuthResponse;
import uz.bank.dto.AuthDtos.ChangePasswordRequest;
import uz.bank.dto.AuthDtos.LoginRequest;
import uz.bank.dto.AuthDtos.RegisterRequest;
import uz.bank.dto.AuthDtos.UpdateProfileRequest;
import uz.bank.dto.AuthDtos.UserResponse;
import uz.bank.security.UserPrincipal;
import uz.bank.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ==========================================
    // VERIFY EMAIL
    // POST /api/auth/verify-email
    // ==========================================
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(
            @RequestBody AuthDtos.VerifyEmailRequest request
    ) {
        authService.verifyEmail(request);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Email muvaffaqiyatli tasdiqlandi"
                )
        );
    }

    // ==========================================
    // REGISTER
    // POST /api/auth/register
    // ==========================================
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(
                authService.register(request)
        );
    }

    // ==========================================
    // LOGIN
    // POST /api/auth/login
    // ==========================================
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    // ==========================================
    // CURRENT USER
    // GET /api/auth/me
    // ==========================================
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                authService.getCurrentUser(principal.getId())
        );
    }

    // ==========================================
    // UPDATE PROFILE
    // PATCH /api/auth/me
    // ==========================================
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateProfileRequest request
    ) {
        return ResponseEntity.ok(
                authService.updateProfile(
                        principal.getId(),
                        request
                )
        );
    }

    // ==========================================
    // CHANGE PASSWORD
    // POST /api/auth/change-password
    // ==========================================
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(
                principal.getId(),
                request
        );

        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // LOGOUT
    // POST /api/auth/logout
    // ==========================================
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}