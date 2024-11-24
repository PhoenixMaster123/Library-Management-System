package app.domain.port;

import app.domain.models.Customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerDao {

    public void addCustomer(Customer customer);
    public Optional<Customer> getCustomer(UUID id);
    void updatePrivileges(Customer customer);
    void updateCustomer(Customer customer);

    void deleteCustomer(UUID id);
}
