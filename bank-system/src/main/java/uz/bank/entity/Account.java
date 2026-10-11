package uz.bank.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "account",
        indexes = {
                @Index(
                        name = "idx_account_card_number",
                        columnList = "card_number"
                ),
                @Index(
                        name = "idx_account_user_id",
                        columnList = "user_id"
                )
        }
)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "card_number",
            nullable = false,
            unique = true,
            length = 20
    )
    private String cardNumber;

    @Column(
            nullable = false,
            precision = 19,
            scale = 2
    )
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(
            nullable = false,
            length = 10
    )
    private String currency = "UZS";

    @Column(
            nullable = false,
            length = 255
    )
    private String owner;

    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    // ==========================================
    // EMPTY CONSTRUCTOR
    // ==========================================

    public Account() {
    }


    // ==========================================
    // CLOUD BACKEND CONSTRUCTOR
    // ==========================================

    public Account(
            User user,
            String owner,
            String cardNumber,
            BigDecimal balance,
            String currency
    ) {
        this.user = user;
        this.owner = owner;
        this.cardNumber = cardNumber;
        this.balance = balance;
        this.currency = currency;
        this.createdAt = LocalDateTime.now();
    }


    // ==========================================
    // OPTIONAL CONSTRUCTOR
    // ==========================================

    public Account(
            String cardNumber,
            BigDecimal balance,
            String currency,
            String owner,
            User user
    ) {
        this.user = user;
        this.owner = owner;
        this.cardNumber = cardNumber;
        this.balance = balance;
        this.currency = currency;
        this.createdAt = LocalDateTime.now();
    }


    // ==========================================
    // PRE PERSIST
    // ==========================================

    @PrePersist
    protected void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (currency == null || currency.isBlank()) {
            currency = "UZS";
        }

        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
    }


    // ==========================================
    // GETTERS
    // ==========================================

    public Long getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getOwner() {
        return owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }


    // ==========================================
    // SETTERS
    // ==========================================

    public void setId(Long id) {
        this.id = id;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUser(User user) {
        this.user = user;
    }
}