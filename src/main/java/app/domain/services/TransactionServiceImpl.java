package app.domain.services;

import app.persistence.repository.BookRepository;
import app.persistence.repository.CustomerRepository;
import app.persistence.repository.TransactionRepository;
import app.persistence.entity.Book;
import app.persistence.entity.Transaction;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.logging.Logger;

@Service
public class TransactionServiceImpl {

    private final TransactionRepository transactionRepository;
    private final BookRepository bookRepository;
    private final CustomerRepository customerRepository;
    private static final Logger LOGGER = Logger.getLogger(TransactionServiceImpl.class.getName());

    public TransactionServiceImpl(TransactionRepository transactionRepository, BookRepository bookRepository, CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.bookRepository = bookRepository;
        this.customerRepository = customerRepository;
    }

    public Transaction borrowBook(Integer customerId, Integer bookId) {
        if (!customerRepository.hasPrivileges(customerId) || !bookRepository.isAvailable(bookId)) {
            throw new RuntimeException("Borrowing not allowed");
        }

        Book book = bookRepository.findById(bookId).orElseThrow(() -> new RuntimeException("Book not found"));
        book.setAvailability(false);
        bookRepository.save(book);

        Transaction transaction = new Transaction();
        transaction.setCustomer(customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Customer not found")));
        transaction.setBook(book);
        transaction.setBorrowDate(LocalDate.now());
        transaction.setDueDate(LocalDate.now().plusDays(14));

        LOGGER.info("Book borrowed by customer " + customerId + ": " + bookId);
        return transactionRepository.save(transaction);
    }

    public Transaction returnBook(Integer transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found"));
        transaction.setReturnDate(LocalDate.now());

        Book book = transaction.getBook();
        book.setAvailability(true);

        bookRepository.save(book);

        LOGGER.info("Book returned for transaction " + transactionId);
        return transactionRepository.save(transaction);
    }

}
