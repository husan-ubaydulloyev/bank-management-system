package uz.bank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_login", columnList = "login"),
                @Index(name = "idx_users_email", columnList = "email")
        }
)

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foydalanuvchining to'liq ismi
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    // Login: phone yoki boshqa login
    @Column(nullable = false, unique = true, length = 150)
    private String login;

    // Hashlangan password
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Email tasdiqlanganmi?
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    // Emailga yuborilgan 6 xonali kod
    @Column(name = "verification_code", length = 10)
    private String verificationCode;

    // Kod qachongacha amal qiladi
    @Column(name = "verification_expires_at")
    private LocalDateTime verificationExpiresAt;

    // User yaratilgan vaqt
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Userning accountlari
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Account> accounts = new ArrayList<>();


    // =========================================================
    // EMPTY CONSTRUCTOR
    // =========================================================

    public User() {
    }


    // =========================================================
    // OLD CONSTRUCTOR
    // Mavjud kodlar buzilmasligi uchun qoldirilgan
    // =========================================================

    public User(
            String fullName,
            String login,
            String passwordHash
    ) {
        this.fullName = fullName;
        this.login = login;
        this.passwordHash = passwordHash;
        this.emailVerified = false;
    }


    // =========================================================
    // NEW CONSTRUCTOR
    // Email bilan register uchun
    // =========================================================

    public User(
            String fullName,
            String login,
            String passwordHash,
            String email
    ) {
        this.fullName = fullName;
        this.login = login;
        this.passwordHash = passwordHash;
        this.email = email;
        this.emailVerified = false;
    }


    // =========================================================
    // PRE PERSIST
    // =========================================================

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (emailVerified) {
            emailVerified = true;
        } else {
            emailVerified = false;
        }
    }


    // =========================================================
    // GETTERS
    // =========================================================

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public LocalDateTime getVerificationExpiresAt() {
        return verificationExpiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Account> getAccounts() {
        return accounts;
    }


    // =========================================================
    // SETTERS
    // =========================================================

//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public void setFullName(String fullName) {
//        this.fullName = fullName;
//    }
//
//    public void setLogin(String login) {
//        this.login = login;
//    }
//
//    public void setPasswordHash(String passwordHash) {
//        this.passwordHash = passwordHash;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public void setEmailVerified(boolean emailVerified) {
//        this.emailVerified = emailVerified;
//    }
//
//    public void setVerificationCode(String verificationCode) {
//        this.verificationCode = verificationCode;
//    }

//    public void setVerificationExpiresAt(
//            LocalDateTime verificationExpiresAt
//    ) {
//        this.verificationExpiresAt = verificationExpiresAt;
//    }
//
//    public void setCreatedAt(LocalDateTime createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    public void setAccounts(List<Account> accounts) {
//        this.accounts = accounts;
//    }


    // =========================================================
    // HELPER METHODS
    // =========================================================

    public boolean isVerificationCodeValid(String code) {

        if (code == null ||
                verificationCode == null ||
                verificationExpiresAt == null) {
            return false;
        }

        if (!verificationCode.equals(code)) {
            return false;
        }

        return LocalDateTime.now()
                .isBefore(verificationExpiresAt);
    }

    public void setVerification(
            String code,
            LocalDateTime expiresAt
    ) {
        this.verificationCode = code;
        this.verificationExpiresAt = expiresAt;
        this.emailVerified = false;
    }

    public void clearVerification() {
        this.verificationCode = null;
        this.verificationExpiresAt = null;
        this.emailVerified = true;
    }
}