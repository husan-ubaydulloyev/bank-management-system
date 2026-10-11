package uz.bank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.bank.entity.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Any transaction where the given account was either the sender or the
    // receiver - i.e. everything relevant to that account's history.
    @Query("select t from Transaction t " +
           "where t.fromAccountId in :accountIds or t.toAccountId in :accountIds " +
           "order by t.createdAt desc")
    List<Transaction> findAllForAccounts(@Param("accountIds") List<Long> accountIds);
}
