package uz.bank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.bank.dto.TransactionDtos.TransactionResponse;
import uz.bank.dto.TransactionDtos.TransferRequest;
import uz.bank.entity.Account;
import uz.bank.entity.Transaction;
import uz.bank.entity.User;
import uz.bank.exception.ApiException;
import uz.bank.repository.AccountRepository;
import uz.bank.repository.TransactionRepository;
import uz.bank.repository.UserRepository;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            UserRepository userRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    // =========================================================
    // LOGIN QILGAN USERNING TRANSACTIONLARI
    // =========================================================
    public List<TransactionResponse> getMyTransactions(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Foydalanuvchi topilmadi"
                        )
                );

        List<Long> accountIds =
                accountRepository
                        .findByUserOrderByCreatedAtDesc(user)
                        .stream()
                        .map(Account::getId)
                        .toList();

        if (accountIds.isEmpty()) {
            return List.of();
        }

        return transactionRepository
                .findAllForAccounts(accountIds)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =========================================================
    // TRANSFER
    // =========================================================
    @Transactional
    public TransactionResponse transfer(
            Long userId,
            TransferRequest request
    ) {

        if (request == null) {
            throw ApiException.badRequest(
                    "Transfer ma'lumotlari kiritilishi shart"
            );
        }

        String senderCard =
                normalize(request.senderAccount());

        String receiverCard =
                normalize(request.receiverAccount());

        if (senderCard.equals(receiverCard)) {
            throw ApiException.badRequest(
                    "Bir xil hisobga o'tkazib bo'lmaydi"
            );
        }

        if (request.amount() == null ||
                request.amount().signum() <= 0) {

            throw ApiException.badRequest(
                    "O'tkazma summasi 0 dan katta bo'lishi kerak"
            );
        }

        /*
         * Ikkala accountni bir xil tartibda lock qilamiz.
         * Bu parallel transferlarda deadlock ehtimolini kamaytiradi.
         */
        String firstCard;
        String secondCard;

        if (senderCard.compareTo(receiverCard) <= 0) {
            firstCard = senderCard;
            secondCard = receiverCard;
        } else {
            firstCard = receiverCard;
            secondCard = senderCard;
        }

        Account firstLocked =
                accountRepository
                        .lockByCardNumber(firstCard)
                        .orElseThrow(() ->
                                ApiException.notFound(
                                        "Jo'natuvchi yoki qabul qiluvchi hisob topilmadi"
                                )
                        );

        Account secondLocked =
                accountRepository
                        .lockByCardNumber(secondCard)
                        .orElseThrow(() ->
                                ApiException.notFound(
                                        "Jo'natuvchi yoki qabul qiluvchi hisob topilmadi"
                                )
                        );

        Account sender;
        Account receiver;

        if (senderCard.equals(firstCard)) {
            sender = firstLocked;
            receiver = secondLocked;
        } else {
            sender = secondLocked;
            receiver = firstLocked;
        }

        // =====================================================
        // USER O'Z ACCOUNTIDAN YUBORYAPTIMI?
        // =====================================================
        if (sender.getUser() == null ||
                sender.getUser().getId() == null ||
                !sender.getUser().getId().equals(userId)) {

            throw ApiException.forbidden(
                    "Bu hisobga ruxsatingiz yo'q"
            );
        }

        // =====================================================
        // BALANS TEKSHIRISH
        // =====================================================
        if (sender.getBalance()
                .compareTo(request.amount()) < 0) {

            throw ApiException.badRequest(
                    "Balans yetarli emas"
            );
        }

        // =====================================================
        // BALANSLARNI O'ZGARTIRISH
        // =====================================================
        sender.setBalance(
                sender.getBalance()
                        .subtract(request.amount())
        );

        receiver.setBalance(
                receiver.getBalance()
                        .add(request.amount())
        );

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // =====================================================
        // TRANSACTION TYPE
        // =====================================================
        String type = request.type();

        if (type == null || type.isBlank()) {
            type = "TRANSFER";
        }

        // =====================================================
        // TRANSACTION YARATISH
        // =====================================================
        Transaction transaction = new Transaction(
                sender.getId(),
                receiver.getId(),
                request.amount(),
                sender.getCurrency(),
                "SUCCESS",
                type,
                sender.getCardNumber(),
                sender.getOwner()
        );

        transactionRepository.save(transaction);

        return toResponse(
                transaction,
                sender.getCardNumber(),
                receiver.getCardNumber()
        );
    }

    // =========================================================
    // KARTA RAQAMINI NORMALIZE QILISH
    // =========================================================
    private String normalize(String cardNumber) {

        if (cardNumber == null ||
                cardNumber.isBlank()) {

            throw ApiException.badRequest(
                    "Karta raqami kiritilishi shart"
            );
        }

        return cardNumber.replaceAll("\\s+", "");
    }

    // =========================================================
    // TRANSACTION -> RESPONSE
    // =========================================================
    private TransactionResponse toResponse(
            Transaction transaction
    ) {
        return toResponse(
                transaction,
                null,
                null
        );
    }

    // =========================================================
    // TRANSACTION -> RESPONSE
    // =========================================================
    private TransactionResponse toResponse(
            Transaction transaction,
            String senderCardOverride,
            String receiverCardOverride
    ) {

        boolean isTransfer =
                transaction.getFromAccountId() != null &&
                        transaction.getToAccountId() != null;

        String senderCard = senderCardOverride;
        String receiverCard = receiverCardOverride;

        // Sender kartasi
        if (senderCard == null &&
                transaction.getFromAccountId() != null) {

            senderCard =
                    accountRepository
                            .findById(
                                    transaction.getFromAccountId()
                            )
                            .map(Account::getCardNumber)
                            .orElse(null);
        }

        // Receiver kartasi
        if (receiverCard == null &&
                transaction.getToAccountId() != null) {

            receiverCard =
                    accountRepository
                            .findById(
                                    transaction.getToAccountId()
                            )
                            .map(Account::getCardNumber)
                            .orElse(null);
        }

        String accountNumber =
                isTransfer
                        ? null
                        : transaction.getCardNumber();

        return new TransactionResponse(
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                accountNumber,
                senderCard,
                receiverCard,
                transaction.getOwner()
        );
    }
}