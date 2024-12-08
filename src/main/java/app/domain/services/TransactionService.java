package app.domain.services;

import app.domain.models.Book;
import app.domain.port.BookDao;
import app.domain.port.CustomerDao;
import app.domain.port.TransactionDao;
import app.adapters.in.dto.CreateNewTransaktion;
import app.domain.models.Customer;
import app.domain.models.Transaction;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {
    private final TransactionDao transactionDao;
    private final BookDao bookDao;
    private final CustomerDao customerDao;

    public TransactionService(TransactionDao transactionDao, BookDao bookDao, CustomerDao customerDao) {
        this.transactionDao = transactionDao;
        this.bookDao = bookDao;
        this.customerDao = customerDao;
    }
    public Transaction createNewTransaction(CreateNewTransaktion newTransaktion) {
        // Validate borrowDate and dueDate
        if (newTransaktion.getBorrowDate() == null || newTransaktion.getDueDate() == null) {
            throw new IllegalArgumentException("Borrow date and due date must not be null.");
        }
        if (newTransaktion.getDueDate().isBefore(newTransaktion.getBorrowDate())) {
            throw new IllegalArgumentException("Due date must be after borrow date.");
        }

        // Fetch customer and book
        Customer customer = customerDao.getCustomer(newTransaktion.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        Book book = bookDao.searchBookById(newTransaktion.getBookId())
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));

        if (!book.isAvailable()) {
            throw new IllegalArgumentException("Book is not available for borrowing.");
        }

        // Mark the book as unavailable
        book.setAvailable(false);
        bookDao.updateBook(book.getBookId(), book);

        // Create the transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setBorrowDate(newTransaktion.getBorrowDate());
        transaction.setDueDate(newTransaktion.getDueDate());
        transaction.setCustomer(customer);
        transaction.setBook(book);

        // Save the transaction
        transactionDao.addTransaction(transaction);
        return transaction;
    }

    public String returnBook(UUID bookId) {
        List<Transaction> transactions = transactionDao.getTransactionsForBook(new Book(bookId, null, null, 0, false, null));

        if (transactions.isEmpty()) {
            throw new EntityNotFoundException("No transaction found for the given book.");
        }

        Transaction transaction = transactions.getFirst(); // Assuming one active transaction per book
        transaction.setReturnDate(LocalDate.now());
        transaction.getBook().setAvailable(true);

        // Persist the updated transaction and book
        transactionDao.updateTransaction(transaction);
        bookDao.updateBook(transaction.getBook().getBookId(), transaction.getBook());

        return transaction.getTransactionId().toString();
    }
    public void borrowBook(UUID customerId, UUID bookId) {
        // Fetch the book and validate availability
        Book book = bookDao.searchBookById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found."));

        if (!book.isAvailable()) {
            throw new RuntimeException("Book is not available for borrowing.");
        }

        // Fetch the customer
        Customer customer = customerDao.getCustomer(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found."));

        if (!customer.isPrivileges()) {
            throw new RuntimeException("Customer does not have borrowing privileges.");
        }

        // Create a new transaction
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setBorrowDate(LocalDate.now());
        transaction.setDueDate(LocalDate.now().plusWeeks(2)); // Example: 2-week loan period
        transaction.setCustomer(customer);
        transaction.setBook(book);

        // Save the transaction
        transactionDao.addTransaction(transaction);

        // Update book availability
        book.setAvailable(false);
        bookDao.updateBook(bookId, book);
    }
    public Page<Transaction> viewBorrowingHistory(UUID customerId, Pageable pageable) {
        return transactionDao.viewBorrowingHistory(customerId, pageable);
    }
    public Optional<Transaction> findById(UUID transactionId) {
        return transactionDao.findById(transactionId);
    }
}
