package app.domain.services;

import app.domain.port.CustomerDao;
import app.adapters.in.dto.CreateNewCustomer;
import app.domain.models.Customer;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
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

    @Cacheable(value = "customers", key = "#id", unless = "#result == null")
    public Optional<Customer> findCustomerById(UUID id) {
        return customerDao.getCustomer(id);
    }
    @Cacheable(value = "customers", key = "#customerName", unless = "#result == null")
    public Optional<Customer> findCustomerByName(String customerName) {
        return customerDao.getCustomerByName(customerName);
    }
    public Page<Customer> getPaginatedCustomers(Pageable pageable) {
        return customerDao.getPaginatedCustomers(pageable);
    }

    public void updatePrivileges(UUID id, boolean privileges) {
        // Fetch the customer and update privileges
        Customer customer = findCustomerById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with ID: " + id));

        customer.setPrivileges(privileges);
        customerDao.updatePrivileges(customer);
    }
    @CachePut(value = "customers", key = "#customer.customerId")
    public void updateCustomer(Customer customer) {
        if (findCustomerById(customer.getCustomerId()).isEmpty()) {
            throw new EntityNotFoundException("Customer not found with ID: " + customer.getCustomerId());
        }
        customerDao.updateCustomer(customer);
    }
    @CacheEvict(value = "customers", key = "#id")
    public void deleteCustomer(UUID id) {
        customerDao.deleteCustomer(id);
    }
}
