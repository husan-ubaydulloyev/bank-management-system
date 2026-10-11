package uz.bank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import uz.bank.dto.TransactionDtos.TransactionResponse;
import uz.bank.dto.TransactionDtos.TransferRequest;
import uz.bank.security.UserPrincipal;
import uz.bank.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService
    ) {
        this.transactionService = transactionService;
    }

    // ==========================================
    // USERNING TRANSACTIONLARI
    // GET /api/transactions
    // ==========================================
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getMyTransactions(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                transactionService.getMyTransactions(
                        principal.getId()
                )
        );
    }

    // ==========================================
    // TRANSFER
    // POST /api/transactions
    // ==========================================
    @PostMapping
    public ResponseEntity<TransactionResponse> transfer(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody TransferRequest request
    ) {
        return ResponseEntity.ok(
                transactionService.transfer(
                        principal.getId(),
                        request
                )
        );
    }
}