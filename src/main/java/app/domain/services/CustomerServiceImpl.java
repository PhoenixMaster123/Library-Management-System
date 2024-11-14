package app.domain.services;

import app.persistence.repository.CustomerRepository;
import app.persistence.entity.Customer;
import app.persistence.entity.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerServiceImpl {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer registerCustomer(Customer customer) {
        customer.setPrivileges(true);  // Grant privileges upon registration
        return customerRepository.save(customer);
    }

    public List<Transaction> viewBorrowingHistory(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return customerRepository.findTransactionsByCustomerId(customerId);
    }
}
