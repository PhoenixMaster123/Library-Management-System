package app.adapters.in.dto;

import app.domain.models.Book;
import app.domain.models.Customer;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateNewTransaktion {
    private LocalDate dueDate;
    private Customer customer;
    private Book book;
}
