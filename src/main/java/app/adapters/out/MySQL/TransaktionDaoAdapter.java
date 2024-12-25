package app.adapters.out.MySQL;

import app.adapters.out.MySQL.entity.TransactionEntity;
import app.adapters.out.MySQL.repositories.BookRepository;
import app.adapters.out.MySQL.repositories.CustomerRepository;
import app.adapters.out.MySQL.repositories.TransactionRepository;
import app.domain.port.TransactionDao;
import app.domain.models.Book;
import app.domain.models.Customer;
import app.domain.models.Transaction;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TransaktionDaoAdapter implements TransactionDao {

    private final TransactionRepository transactionRepository;
    private final BookRepository bookRepository;
    private final CustomerRepository customerRepository;

    public TransaktionDaoAdapter(TransactionRepository transactionRepository, BookRepository bookRepository, CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.bookRepository = bookRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public void addTransaction(Transaction transaction) {
        TransactionEntity transactionEntity = new TransactionEntity();
        // Do not set transactionId manually, it's handled by JPA
        transactionEntity.setBorrowDate(transaction.getBorrowDate());
        transactionEntity.setReturnDate(transaction.getReturnDate());
        transactionEntity.setDueDate(transaction.getDueDate());
        transactionEntity.setCustomer(customerRepository.findById(transaction.getCustomer().getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found")));
        transactionEntity.setBook(bookRepository.findById(transaction.getBook().getBookId())
                .orElseThrow(() -> new EntityNotFoundException("Book not found")));

        transactionRepository.save(transactionEntity);
    }

    @Override
    public List<Transaction> getTransactionsForBook(Book book) {
        return transactionRepository.findByBookBookId(book.getBookId())
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Transaction> viewBorrowingHistory(UUID customerID, Pageable pageable) {
        return transactionRepository.findByCustomerCustomerId(customerID, pageable)
                .map(this::mapToDomain);
    }

    @Override
    public Optional<Transaction> findById(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .map(this::mapToDomain);
    }
    @Override
    public void updateTransaction(Transaction transaction) {
        TransactionEntity entity = transactionRepository.findById(transaction.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
        entity.setReturnDate(transaction.getReturnDate());
        entity.setDueDate(transaction.getDueDate());
        transactionRepository.save(entity);
    }

    private Transaction mapToDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getTransactionId(),
                entity.getBorrowDate(),
                entity.getReturnDate(),
                entity.getDueDate(),
                new Customer(
                        entity.getCustomer().getCustomerId(),
                        entity.getCustomer().getName(),
                        entity.getCustomer().getEmail(),
                        entity.getCustomer().isPrivileges()
                ),
                new Book(
                        entity.getBook().getBookId(),
                        entity.getBook().getTitle(),
                        entity.getBook().getIsbn(),
                        entity.getBook().getPublicationYear(),
                        entity.getBook().isAvailability(),
                        entity.getBook().getCreated_at()
                )
        );
    }
}
