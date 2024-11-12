package app.persistence.repository;


import app.persistence.entity.Customer;
import app.persistence.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    // TODO: Make it compliant to JPA method naming convention
    boolean hasPrivileges(Integer customerId);

    // TODO: Make it compliant to JPA method naming convention
    List<Transaction> findBorrowingHistory(Integer customerId);
}
