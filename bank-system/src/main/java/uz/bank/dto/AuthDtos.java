package uz.bank.dto;

public final class AuthDtos {

    private AuthDtos() {
    }

    // =========================================================
    // REGISTER
    // =========================================================
    public record RegisterRequest(
            String fullName,
            String login,
            String email,
            String password
    ) {

    }
    public record VerifyEmailRequest(
            String email,
            String code
    ) {}


    // =========================================================
    // LOGIN
    // =========================================================
    public record LoginRequest(
            String login,
            String password
    ) {
    }

    // =========================================================
    // AUTH RESPONSE
    // =========================================================
    public record AuthResponse(
            String token,
            UserResponse user
    ) {
    }

    // =========================================================
    // USER RESPONSE
    // =========================================================
    public record UserResponse(
            Long id,
            String fullName,
            String login
    ) {
    }

    // =========================================================
    // UPDATE PROFILE
    // =========================================================
    public record UpdateProfileRequest(
            String fullName,
            String login
    ) {
    }

    // =========================================================
    // CHANGE PASSWORD
    // =========================================================
    public record ChangePasswordRequest(
            String oldPassword,
            String newPassword
    ) {
    }
}