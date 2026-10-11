package uz.bank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.bank.dto.AccountDtos.AccountResponse;
import uz.bank.dto.AccountDtos.DepositRequest;
import uz.bank.dto.AccountDtos.WithdrawRequest;
import uz.bank.entity.Account;
import uz.bank.entity.Transaction;
import uz.bank.entity.User;
import uz.bank.exception.ApiException;
import uz.bank.repository.AccountRepository;
import uz.bank.repository.TransactionRepository;
import uz.bank.repository.UserRepository;
import uz.bank.util.CardNumberGenerator;
import uz.bank.dto.AccountDtos;
import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository,
                           UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public List<AccountResponse> getMyAccounts(Long userId) {
        User user = getUser(userId);
        return accountRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AccountDtos.ReceiverResponse findReceiver(String cardNumber) {

        String normalizedCard = cardNumber == null
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
    @Transactional
    public AccountResponse createAdditionalAccount(Long userId) {
        User user = getUser(userId);

        String cardNumber;
        do {
            cardNumber = CardNumberGenerator.generate();
        } while (accountRepository.existsByCardNumber(cardNumber));

        Account account = new Account(user, user.getFullName(), cardNumber, BigDecimal.ZERO, "UZS");
        return toResponse(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse deposit(Long userId, DepositRequest request) {
        Account account = lockOwnedAccount(userId, request.cardNumber());

        account.setBalance(account.getBalance().add(request.amount()));
        accountRepository.save(account);

        Transaction transaction = new Transaction(
                null, account.getId(),
                request.amount(), account.getCurrency(),
                "SUCCESS", "DEPOSIT",
                account.getCardNumber(), account.getOwner()
        );
        transactionRepository.save(transaction);

        return toResponse(account);
    }

    @Transactional
    public AccountResponse withdraw(Long userId, WithdrawRequest request) {
        Account account = lockOwnedAccount(userId, request.cardNumber());

        if (account.getBalance().compareTo(request.amount()) < 0) {
            throw ApiException.badRequest("Balans yetarli emas");
        }

        account.setBalance(account.getBalance().subtract(request.amount()));
        accountRepository.save(account);

        Transaction transaction = new Transaction(
                account.getId(), null,
                request.amount(), account.getCurrency(),
                "SUCCESS", "WITHDRAW",
                account.getCardNumber(), account.getOwner()
        );
        transactionRepository.save(transaction);

        return toResponse(account);
    }

    // Locks the account row (prevents concurrent balance races) and verifies
    // the authenticated user actually owns it.
    Account lockOwnedAccount(Long userId, String cardNumber) {
        Account account = accountRepository.lockByCardNumber(cardNumber)
                .orElseThrow(() -> ApiException.notFound("Hisob topilmadi"));

        if (account.getUser() == null || !account.getUser().getId().equals(userId)) {
            throw ApiException.forbidden("Bu hisobga ruxsatingiz yo'q");
        }

        return account;
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("Foydalanuvchi topilmadi"));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getOwner(),
                account.getCardNumber(),
                account.getBalance(),
                account.getCurrency(),
                account.getCreatedAt()
        );
    }
}
