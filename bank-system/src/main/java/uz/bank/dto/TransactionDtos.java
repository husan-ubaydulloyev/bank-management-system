package uz.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class TransactionDtos {

    private TransactionDtos() {
    }

    // =========================================================
    // TRANSFER REQUEST
    // Frontenddan keladigan o'tkazma
    // =========================================================
    public record TransferRequest(
            String senderAccount,
            String receiverAccount,
            BigDecimal amount,
            String type
    ) {
    }

    // =========================================================
    // TRANSACTION RESPONSE
    // Frontendga qaytariladigan transaction
    // =========================================================
    public record TransactionResponse(
            Long id,
            String type,
            BigDecimal amount,
            String currency,
            String status,
            LocalDateTime createdAt,
            String accountNumber,
            String senderAccount,
            String receiverAccount,
            String owner
    ) {
    }
}