package app.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class Customer {
    private Integer customerId;
    private String name;
    private String email;
    private boolean privileges;
    private final List<Transaction> transactions = new LinkedList<>();

    public Customer(Integer customerId, String name, String email, boolean privileges) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.privileges = privileges;
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void removeTransaction(Transaction transaction) {
        transactions.remove(transaction);
    }
}
