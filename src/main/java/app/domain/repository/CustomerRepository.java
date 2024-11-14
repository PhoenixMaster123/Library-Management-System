package app.domain.repository;


import app.persistence.entity.Customer;
import app.persistence.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    boolean existsByCustomerIdAndPrivileges(Integer customerId, boolean privileges);
    //List<Transaction> findBorrowingHistory(Integer customerId);
    List<Transaction> findTransactionsByCustomerId(Integer customerId);
}
