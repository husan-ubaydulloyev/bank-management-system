package uz.bank.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uz.bank.dto.AccountDtos;
import uz.bank.dto.AuthDtos.AuthResponse;
import uz.bank.dto.AuthDtos.ChangePasswordRequest;
import uz.bank.dto.AuthDtos.LoginRequest;
import uz.bank.dto.AuthDtos.RegisterRequest;
import uz.bank.dto.AuthDtos.UpdateProfileRequest;
import uz.bank.dto.AuthDtos.UserResponse;
import uz.bank.dto.AuthDtos.VerifyEmailRequest;

import uz.bank.entity.Account;
import uz.bank.entity.User;

import uz.bank.exception.ApiException;

import uz.bank.repository.AccountRepository;
import uz.bank.repository.UserRepository;

import uz.bank.security.JwtService;
import uz.bank.util.CardNumberGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    // =========================================================
    // REGISTER
    // =========================================================

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (request == null) {
            throw ApiException.badRequest(
                    "Ro'yxatdan o'tish ma'lumotlari kiritilishi shart"
            );
        }

        String fullName = request.fullName() == null
                ? ""
                : request.fullName().trim();

        String login = request.login() == null
                ? ""
                : request.login().trim();

        String email = request.email() == null
                ? ""
                : request.email().trim().toLowerCase();

        String password = request.password();

        // =====================================================
        // VALIDATION
        // =====================================================

        if (fullName.isBlank()) {
            throw ApiException.badRequest(
                    "Ism va familiya kiritilishi shart"
            );
        }

        if (login.isBlank()) {
            throw ApiException.badRequest(
                    "Login kiritilishi shart"
            );
        }

        if (email.isBlank()) {
            throw ApiException.badRequest(
                    "Email kiritilishi shart"
            );
        }

        if (password == null || password.length() < 6) {
            throw ApiException.badRequest(
                    "Parol kamida 6 ta belgidan iborat bo'lishi kerak"
            );
        }

        // =====================================================
        // LOGIN TEKSHIRISH
        // =====================================================

        if (userRepository.existsByLogin(login)) {
            throw ApiException.conflict(
                    "Bu login bilan foydalanuvchi allaqachon mavjud"
            );
        }

        // =====================================================
        // EMAIL TEKSHIRISH
        // =====================================================

        if (userRepository.existsByEmail(email)) {
            throw ApiException.conflict(
                    "Bu email bilan foydalanuvchi allaqachon mavjud"
            );
        }

        // =====================================================
        // VERIFICATION CODE
        // =====================================================

        String verificationCode = String.format(
                "%06d",
                ThreadLocalRandom.current().nextInt(1_000_000)
        );

        LocalDateTime verificationExpiresAt =
                LocalDateTime.now().plusMinutes(10);

        // =====================================================
        // USER
        // =====================================================

        User user = new User(
                fullName,
                login,
                passwordEncoder.encode(password),
                email
        );

        user.setVerification(
                verificationCode,
                verificationExpiresAt
        );

        user = userRepository.save(user);

        // =====================================================
        // DEFAULT ACCOUNT
        // =====================================================

        String cardNumber;

        do {
            cardNumber = CardNumberGenerator.generate();
        } while (accountRepository.existsByCardNumber(cardNumber));

        Account account = new Account(
                user,
                user.getFullName(),
                cardNumber,
                BigDecimal.ZERO,
                "UZS"
        );

        accountRepository.save(account);

        // =====================================================
        // REAL EMAIL vaqtincha ochirildi
        // =====================================================

//        emailService.sendVerificationCode(
//                email,
//                verificationCode
//        );
//
//        System.out.println(
//                "Verification code yuborildi: " + email
//        );

        // =====================================================
        // RESPONSE
        // =====================================================

        String token = jwtService.generateToken(
                user.getId(),
                user.getLogin()
        );

        return new AuthResponse(
                token,
                toUserResponse(user)
        );
    }

    // =========================================================
    // VERIFY EMAIL
    // =========================================================

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {

        if (request == null) {
            throw ApiException.badRequest(
                    "Verification ma'lumotlari kiritilishi shart"
            );
        }

        String email = request.email() == null
                ? ""
                : request.email().trim().toLowerCase();

        String code = request.code() == null
                ? ""
                : request.code().trim();

        if (email.isBlank()) {
            throw ApiException.badRequest(
                    "Email kiritilishi shart"
            );
        }

        if (code.isBlank()) {
            throw ApiException.badRequest(
                    "Verification code kiritilishi shart"
            );
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Bu email bilan foydalanuvchi topilmadi"
                        )
                );

        if (user.isEmailVerified()) {
            throw ApiException.badRequest(
                    "Email allaqachon tasdiqlangan"
            );
        }

        if (!user.isVerificationCodeValid(code)) {
            throw ApiException.badRequest(
                    "Verification code noto'g'ri yoki muddati tugagan"
            );
        }

        user.clearVerification();

        userRepository.save(user);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    public AuthResponse login(LoginRequest request) {

        if (request == null ||
                request.login() == null ||
                request.password() == null) {

            throw ApiException.unauthorized(
                    "Login yoki parol noto'g'ri"
            );
        }

        String login = request.login().trim();

        System.out.println("LOGIN KELDI: " + login);

        User user = userRepository
                .findByLogin(login)
                .orElseThrow(() ->
                        ApiException.unauthorized(
                                "Login yoki parol noto'g'ri"
                        )
                );

        System.out.println("USER TOPILDI: " + user.getLogin());
        System.out.println("USER ID: " + user.getId());

        boolean passwordCorrect = passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        );

        System.out.println("PASSWORD TO'G'RI: " + passwordCorrect);

        if (!passwordCorrect) {
            throw ApiException.unauthorized(
                    "Login yoki parol noto'g'ri"
            );
        }

    /*
    Hozircha email verification loginni bloklamaydi.

    if (!user.isEmailVerified()) {
        throw ApiException.forbidden(
                "Avval emailingizni tasdiqlang"
        );
    }
    */

        String token = jwtService.generateToken(
                user.getId(),
                user.getLogin()
        );

        return new AuthResponse(
                token,
                toUserResponse(user)
        );
    }
    // =========================================================
    // CURRENT USER
    // =========================================================

    public UserResponse getCurrentUser(Long userId) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Foydalanuvchi topilmadi"
                        )
                );

        return toUserResponse(user);
    }

    // =========================================================
    // UPDATE PROFILE
    // =========================================================

    @Transactional
    public UserResponse updateProfile(
            Long userId,
            UpdateProfileRequest request
    ) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Foydalanuvchi topilmadi"
                        )
                );

        if (request.fullName() != null &&
                !request.fullName().isBlank()) {

            user.setFullName(
                    request.fullName().trim()
            );
        }

        userRepository.save(user);

        return toUserResponse(user);
    }

    // =========================================================
    // FIND RECEIVER BY CARD
    // =========================================================

    @Transactional(readOnly = true)
    public AccountDtos.ReceiverResponse findReceiver(
            String cardNumber
    ) {

        String normalizedCard =
                cardNumber == null
                        ? ""
                        : cardNumber.replaceAll("\\s+", "");

        if (normalizedCard.isBlank()) {
            throw ApiException.badRequest(
                    "Karta raqami kiritilishi shart"
            );
        }

        Account account = accountRepository
                .findByCardNumber(normalizedCard)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Qabul qiluvchi karta topilmadi"
                        )
                );

        return new AccountDtos.ReceiverResponse(
                account.getId(),
                account.getOwner(),
                account.getCardNumber()
        );
    }

    // =========================================================
    // CHANGE PASSWORD
    // =========================================================

    @Transactional
    public void changePassword(
            Long userId,
            ChangePasswordRequest request
    ) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Foydalanuvchi topilmadi"
                        )
                );

        if (request.oldPassword() == null ||
                request.newPassword() == null) {

            throw ApiException.badRequest(
                    "Parol ma'lumotlari to'liq emas"
            );
        }

        if (!passwordEncoder.matches(
                request.oldPassword(),
                user.getPasswordHash()
        )) {

            throw ApiException.badRequest(
                    "Joriy parol noto'g'ri"
            );
        }

        if (request.newPassword().length() < 6) {

            throw ApiException.badRequest(
                    "Yangi parol kamida 6 ta belgidan iborat bo'lishi kerak"
            );
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        userRepository.save(user);
    }

    // =========================================================
    // USER RESPONSE
    // =========================================================

    private UserResponse toUserResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getLogin()
        );
    }
}