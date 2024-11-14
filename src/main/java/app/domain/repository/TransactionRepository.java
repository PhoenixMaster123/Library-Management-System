package app.domain.repository;

import app.persistence.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findByCustomerCustomerId(Integer customerId);

    List<Transaction> findByBookBookId(Integer bookId);

    List<Transaction> findByCustomerCustomerIdAndReturnDateIsNull(Integer customerId);
    List<Transaction> findByDueDateBeforeAndReturnDateIsNull(LocalDate currentDate);
}
