package app.domain.port;

import app.adapters.out.MySQL.entity.TransactionEntity;
import app.domain.models.Book;
import app.domain.models.Customer;
import app.domain.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionDao {
    void addTransaction(Transaction transaction);

    List<Transaction> getTransactionsForBook(Book book);
    Page<Transaction> viewBorrowingHistory(UUID customerId, Pageable pageable);
    Optional<Transaction> findById(UUID transactionId);

    List<Transaction> findAll();

    void updateTransaction(Transaction transaction);
    void borrowBook(Transaction transaction);
    void returnBook(UUID transactionId, LocalDate returnDate);
}
