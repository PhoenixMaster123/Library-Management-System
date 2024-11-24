package app.adapters.out.MySQL.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "customers")
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID customerId;
    private String name;
    private String email;
    private boolean privileges;

    @OneToMany(mappedBy = "customer")
    private List<TransactionEntity> transactions = new ArrayList<>();

    public CustomerEntity() {

    }
}
