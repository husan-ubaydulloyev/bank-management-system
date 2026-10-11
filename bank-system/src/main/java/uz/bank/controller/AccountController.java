package uz.bank.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import uz.bank.dto.AccountDtos;
import uz.bank.dto.AccountDtos.AccountResponse;
import uz.bank.dto.AccountDtos.DepositRequest;
import uz.bank.dto.AccountDtos.WithdrawRequest;
import uz.bank.security.UserPrincipal;
import uz.bank.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // =========================================================
    // GET /api/accounts
    // Login qilgan userning accountlari
    // =========================================================

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                accountService.getMyAccounts(
                        principal.getId()
                )
        );
    }

    // =========================================================
    // GET /api/accounts/by-card/{cardNumber}
    // Receiver'ni karta raqami orqali topish
    // =========================================================

    @GetMapping("/by-card/{cardNumber}")
    public ResponseEntity<AccountDtos.ReceiverResponse> findReceiver(
            @PathVariable String cardNumber
    ) {
        return ResponseEntity.ok(
                accountService.findReceiver(cardNumber)
        );
    }

    // =========================================================
    // POST /api/accounts
    // Yangi account
    // =========================================================

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                accountService.createAdditionalAccount(
                        principal.getId()
                )
        );
    }

    // =========================================================
    // POST /api/accounts/deposit
    // =========================================================

    @PostMapping("/deposit")
    public ResponseEntity<AccountResponse> deposit(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DepositRequest request
    ) {
        return ResponseEntity.ok(
                accountService.deposit(
                        principal.getId(),
                        request
                )
        );
    }

    // =========================================================
    // POST /api/accounts/withdraw
    // =========================================================

    @PostMapping("/withdraw")
    public ResponseEntity<AccountResponse> withdraw(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody WithdrawRequest request
    ) {
        return ResponseEntity.ok(
                accountService.withdraw(
                        principal.getId(),
                        request
                )
        );
    }
}