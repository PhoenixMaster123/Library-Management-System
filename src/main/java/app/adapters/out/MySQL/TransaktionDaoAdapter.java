package app.adapters.out.MySQL;

import app.adapters.out.MySQL.entity.BookEntity;
import app.adapters.out.MySQL.entity.CustomerEntity;
import app.adapters.out.MySQL.entity.TransactionEntity;
import app.adapters.out.MySQL.repositories.TransactionRepository;
import app.domain.port.TransactionDao;
import app.domain.models.Book;
import app.domain.models.Customer;
import app.domain.models.Transaction;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TransaktionDaoAdapter implements TransactionDao {

    private TransactionRepository transactionRepository;

    public TransaktionDaoAdapter(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void addTransaction(Transaction transaction) {
        TransactionEntity transactionEntity = TransactionEntity.builder()
                .transactionId(transaction.getTransactionId())
                .borrowDate(transaction.getBorrowDate())
                .returnDate(transaction.getReturnDate())
                .dueDate(transaction.getDueDate())
                .customer(CustomerEntity.builder()
                        .customerId(transaction.getCustomer().getCustomerId())
                        .build())
                .book(BookEntity.builder()
                        .bookId(transaction.getBook().getBookId())
                        .build())
                .build();
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
    public List<Transaction> viewBorrowingHistory(Customer customer) {
        return transactionRepository.findByCustomerCustomerId(customer.getCustomerId())  // Closing parenthesis here
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Transaction> findById(UUID transactionId) {
        return transactionRepository.findById(transactionId)
                .map(this::mapToDomain);
    }

    @Override
    public List<Transaction> findAll() {
        return transactionRepository.findAll().stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void updateTransaction(Transaction transaction) {
        TransactionEntity entity = transactionRepository.findById(transaction.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
        entity.setReturnDate(transaction.getReturnDate());
        entity.setDueDate(transaction.getDueDate());
        transactionRepository.save(entity);
    }

    @Override
    public List<Transaction> findByBookId(UUID bookId) {
        return transactionRepository.findByBookBookId(bookId)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByCustomerId(UUID customerId) {
        return transactionRepository.findByCustomerCustomerId(customerId)
                .stream()
                .map(this::mapToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void borrowBook(Transaction transaction) {
        transaction.setBorrowDate(LocalDate.now());
        transaction.setDueDate(LocalDate.now().plusWeeks(2)); // Example: 2-week loan period
        addTransaction(transaction);
    }

    @Override
    public void returnBook(UUID transactionId, LocalDate returnDate) {
        TransactionEntity transactionEntity = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
        transactionEntity.setReturnDate(returnDate);
        transactionRepository.save(transactionEntity);
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
