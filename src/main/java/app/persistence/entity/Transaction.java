package app.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private LocalDate dueDate;

    @ManyToOne
    private Customer customer;

    @ManyToOne
    private Book book;
}
