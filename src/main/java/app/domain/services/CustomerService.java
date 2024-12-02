package app.domain.services;

import app.domain.port.CustomerDao;
import app.adapters.in.dto.CreateNewCustomer;
import app.domain.models.Customer;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerDao customerDao;

    public CustomerService(CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public Customer createNewCustomer(CreateNewCustomer createNewCustomer) {
        Customer customer = new Customer(null, createNewCustomer.getName(), createNewCustomer.getEmail(), true);
        customerDao.addCustomer(customer);
        return customer;
    }

    public Optional<Customer> findCustomerById(UUID id) {
        return customerDao.getCustomer(id);
    }
    public Optional<Customer> findCustomerByName(String customerName) {
        return customerDao.getCustomerByName(customerName);
    }

    @Transactional
    public void updatePrivileges(UUID id, boolean privileges) {
        // Fetch the customer and update privileges
        Customer customer = findCustomerById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + id));

        customer.setPrivileges(privileges);
        customerDao.updatePrivileges(customer);
    }
    public void updateCustomer(Customer customer) {
        if (findCustomerById(customer.getCustomerId()).isEmpty()) {
            throw new EntityNotFoundException("Customer not found with ID: " + customer.getCustomerId());
        }
        customerDao.updateCustomer(customer);
    }

    public void deleteCustomer(UUID id) {
        customerDao.deleteCustomer(id);
    }
}
