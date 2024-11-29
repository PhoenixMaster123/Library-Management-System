package app.domain.port;

import app.domain.models.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerDao {

    void addCustomer(Customer customer);
    Optional<Customer> getCustomer(UUID id);
    Optional<Customer> getCustomerByName(String name);
    void updatePrivileges(Customer customer);
    void updateCustomer(Customer customer);
    void deleteCustomer(UUID id);
}
