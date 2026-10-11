package uz.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import uz.bank.entity.Account;
import uz.bank.entity.User;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUserOrderByCreatedAtDesc(User user);

    Optional<Account> findByCardNumber(String cardNumber);

    boolean existsByCardNumber(String cardNumber);

    // Pessimistic lock used while a deposit/withdraw/transfer mutates the
    // balance, so two concurrent requests against the same account can't
    // both read a stale balance and push it negative.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.cardNumber = :cardNumber")
    Optional<Account> lockByCardNumber(@Param("cardNumber") String cardNumber);
}
