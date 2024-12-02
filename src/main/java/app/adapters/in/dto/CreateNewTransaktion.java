package app.adapters.in.dto;

import app.domain.models.Book;
import app.domain.models.Customer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateNewTransaktion {
    private UUID transactionId;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private LocalDate dueDate;
    private UUID customerId;
    private UUID bookId;
}
