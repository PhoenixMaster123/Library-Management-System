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

    // Constructor for new customers
    public Customer(String name, String email, boolean privileges) {
        this(UUID.randomUUID(), name, email, privileges);
    }

    // Constructor for existing customers
    public Customer(UUID customerId, String name, String email, boolean privileges) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.privileges = privileges;
    }
    public Customer() {

    }
}
