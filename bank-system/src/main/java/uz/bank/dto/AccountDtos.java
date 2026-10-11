package uz.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class AccountDtos {

    private AccountDtos() {
    }

    // =========================================================
    // ACCOUNT RESPONSE
    // =========================================================
    public record AccountResponse(
            Long id,
            String ownerName,
            String cardNumber,
            BigDecimal balance,
            String currency,
            LocalDateTime createdAt
    ) {
    }

    // =========================================================
    // CREATE ACCOUNT REQUEST
    // =========================================================
    public record CreateAccountRequest(
            String cardNumber,
            String ownerName,
            BigDecimal balance,
            String currency
    ) {
    }

    // =========================================================
    // DEPOSIT REQUEST
    // =========================================================
    public record DepositRequest(
            String cardNumber,
            BigDecimal amount
    ) {
    }

    // =========================================================
    // WITHDRAW REQUEST
    // =========================================================
    public record WithdrawRequest(
            String cardNumber,
            BigDecimal amount
    ) {
    }

    public record ReceiverResponse(
            Long id,
            String ownerName,
            String cardNumber
    ) {
    }
}