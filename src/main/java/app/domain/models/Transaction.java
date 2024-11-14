package app.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
public class Transaction {
    private Integer transactionId;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private LocalDate dueDate;
    private Customer customer;
    private Book book;

    public Transaction(Integer transactionId, LocalDate borrowDate, LocalDate dueDate, Customer customer, Book book) {
        this.transactionId = transactionId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.customer = customer;
        this.book = book;
    }
    public boolean isOverdue() {
        return returnDate == null && LocalDate.now().isAfter(dueDate);
    }
}
