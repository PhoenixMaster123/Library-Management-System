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
    public Transaction(UUID transactionId, LocalDate borrowDate, LocalDate returnDate, LocalDate dueDate, Customer customer, Book book) {
        this.transactionId = transactionId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.dueDate = dueDate;
        this.customer = customer;
        this.book = book;
    }
    public Transaction() {

    }
}
