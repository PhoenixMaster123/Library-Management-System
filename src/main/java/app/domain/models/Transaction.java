package app.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class Transaction {
    private UUID transactionId;
    private UUID customerId;
    private UUID bookId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Customer customer;
    private Book book;

    public Transaction(UUID customerId, UUID bookId, LocalDate borrowDate, LocalDate dueDate) {
        this.transactionId = UUID.randomUUID();
        this.customerId = customerId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }
    public Transaction(UUID transactionId, LocalDate borrowDate, LocalDate returnDate, LocalDate dueDate, Customer customer, Book book) {
        this.transactionId = transactionId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.dueDate = dueDate;
        this.customer = customer;
        this.book = book;
    }
    public Transaction(LocalDate borrowDate, LocalDate dueDate, Customer customer, Book book) {
        this.transactionId = UUID.randomUUID();
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.customer = customer;
        this.book = book;
    }
    public Transaction() {

    }

    public Transaction(UUID transactionId, LocalDate borrowDate, LocalDate returnDate) {
        this.transactionId = transactionId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
    }

    public boolean isOverdue() {
        return returnDate == null && LocalDate.now().isAfter(dueDate);
    }
}
