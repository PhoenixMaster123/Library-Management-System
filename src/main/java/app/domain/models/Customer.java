package app.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;


@Getter
@Setter
public class Customer {
    private UUID customerId;
    private String name;
    private String email;
    private boolean privileges;
    private final List<Transaction> transactions = new LinkedList<>();

    // Constructor for existing customers
    public Customer(UUID customerId, String name, String email, boolean privileges) {
        this.customerId = customerId; // JPA will handle ID assignment for new customers
        this.name = name;
        this.email = email;
        this.privileges = privileges;
    }
    public Customer() {

    }
}
