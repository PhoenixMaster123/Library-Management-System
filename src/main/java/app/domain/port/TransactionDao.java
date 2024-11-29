package app.domain.port;

import app.adapters.out.MySQL.entity.TransactionEntity;
import app.domain.models.Book;
import app.domain.models.Customer;
import app.domain.models.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionDao {
    void addTransaction(Transaction transaction);

    List<Transaction> getTransactionsForBook(Book book);
    List<Transaction> viewBorrowingHistory(Customer customer);
    Optional<Transaction> findById(UUID transactionId);

    List<Transaction> findAll();

    void updateTransaction(Transaction transaction);
    List<Transaction> findByBookId(UUID bookId);
    List<Transaction> findByCustomerId(UUID customerId);
    void borrowBook(Transaction transaction);
    void returnBook(UUID transactionId, LocalDate returnDate);
}
