package app.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Id;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;
    private String name;
    private String email;
    private boolean privileges;

    @OneToMany(mappedBy = "customer")
    private List<Transaction> transactions = new ArrayList<>();

    public boolean isPrivileges() {
        return privileges;
    }

    public void setPrivileges(boolean privileges) {
        this.privileges = privileges;
    }
}
