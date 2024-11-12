package app.persistence.repository;

import app.persistence.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findByCustomerCustomerId(Integer customerId);

    List<Transaction> findByBookBookId(Integer bookId);

    // TODO: Make it compliant to JPA method naming convention
    List<Transaction> findActiveTransactionsByCustomerId(Integer customerId);

    // TODO: Make it compliant to JPA method naming convention
    List<Transaction> findOverdueTransactions(LocalDate currentDate);
}
