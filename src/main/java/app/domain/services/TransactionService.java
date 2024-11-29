package app.domain.services;

import app.domain.models.Book;
import app.domain.port.BookDao;
import app.domain.port.TransactionDao;
import app.adapters.in.dto.CreateNewTransaktion;
import app.domain.models.Customer;
import app.domain.models.Transaction;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionDao transactionDao;
    private BookDao bookDao;

    public TransactionService(TransactionDao transactionDao, BookDao bookDao) {
        this.transactionDao = transactionDao;
        this.bookDao = bookDao;
    }
    public Transaction createNewTransaction(CreateNewTransaktion createNewTransaction) {
        // Validate book availability
        Book book = createNewTransaction.getBook();
        if (!book.isAvailable()) {
            throw new IllegalArgumentException("Book is not available for borrowing.");
        }

        // Create new transaction
        Transaction transaction = new Transaction(
                LocalDate.now(),
                createNewTransaction.getDueDate(),
                createNewTransaction.getCustomer(),
                book
        );

        // Add transaction and update book status
        transactionDao.addTransaction(transaction);
        book.setAvailable(false);
        return transaction;
    }
    public List<Transaction> getOverdueTransactions() {
        List<Transaction> allTransactions = transactionDao.findAll();
        return allTransactions.stream()
                .filter(Transaction::isOverdue)
                .collect(Collectors.toList());
    }

    public String returnBook(UUID transactionId) {
        Transaction transaction = transactionDao.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        transaction.setReturnDate(LocalDate.now()); // Set return date to today

        // Mark the book as available again
        transaction.getBook().setAvailable(true);

        // Apply penalty if overdue
        if (transaction.isOverdue()) {
            System.out.println("Late fee applied for transaction ID: " + transaction.getTransactionId());
            // Add penalty logic here
        }

        transactionDao.updateTransaction(transaction);
        return "Book returned successfully: " + transaction.getTransactionId();
    }
    public void borrowBook(UUID customerId, UUID bookId) {
        bookDao.searchByIsbn(bookId.toString()).ifPresent(book -> {
            if (!book.isAvailable()) {
                throw new RuntimeException("Book is not available for borrowing.");
            }

            Transaction transaction = new Transaction(
                    customerId,
                    bookId,
                    LocalDate.now(),
                    LocalDate.now().plusWeeks(2) // 2-week borrowing period
            );

            transactionDao.borrowBook(transaction);

            // Update book availability
            book.setAvailable(false);
            bookDao.updateBook(bookId, book);
        });
    }
    public List<Transaction> viewBorrowingHistory(UUID customerId) {
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        return transactionDao.viewBorrowingHistory(customer);
    }
    public void sendOverdueNotifications() {
        List<Transaction> overdueTransactions = getOverdueTransactions();
        overdueTransactions.forEach(transaction -> {
            Customer customer = transaction.getCustomer();
            String message = "Dear " + customer.getName() + ", your transaction for book '"
                    + transaction.getBook().getTitle() + "' is overdue. Please return it immediately.";
            // Assume notificationService sends emails or notifications
            sendNotification(customer.getEmail(), message);
        });
    }
    private void sendNotification(String email, String message) {
        System.out.println("Sending notification to " + email + ": " + message);
        // Real notification logic would go here
    }



    ////////////////////////////////////////////////////////////////////////////////


/*private final TransactionRepository transactionRepository;
    private final Book bookRepository;
    private final CustomerRepository customerRepository;
    private static final Logger LOGGER = Logger.getLogger(TransactionServiceImpl.class.getName());

    public TransactionServiceImpl(TransactionRepository transactionRepository, Book bookRepository, CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.bookRepository = bookRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Transaction borrowBook(Integer customerId, Integer bookId) {
        if (!customerRepository.existsByCustomerIdAndPrivileges(customerId, true) || !bookRepository.existsByBookIdAndAvailability(bookId, true)) {
            throw new RuntimeException("Borrowing not allowed");
        }

        app.persistence.entity.Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));
        book.setAvailability(false);  // Assuming a `setAvailable` method in the Book entity
        bookRepository.save(book);

        Transaction transaction = new Transaction();
        transaction.setCustomer(customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found")));
        transaction.setBook(book);
        transaction.setBorrowDate(LocalDate.now());
        transaction.setDueDate(LocalDate.now().plusDays(14));

        LOGGER.info("Book borrowed by customer " + customerId + ": " + bookId);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction returnBook(Integer transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found"));
        transaction.setReturnDate(LocalDate.now());

        app.persistence.entity.Book book = transaction.getBook();
        book.setAvailability(true);  // Assuming a `setAvailable` method in the Book entity
        bookRepository.save(book);

        LOGGER.info("Book returned for transaction " + transactionId);
        return transactionRepository.save(transaction);
    }*/
}
