package app.adapters.out.MySQL;

import app.adapters.out.MySQL.entity.CustomerEntity;
import app.adapters.out.MySQL.repositories.CustomRepository;
import app.domain.port.CustomerDao;
import app.domain.models.Customer;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CustomerDaoAdapter implements CustomerDao {
    private final CustomRepository customRepository;

    public CustomerDaoAdapter(CustomRepository customRepository) {
        this.customRepository = customRepository;
    }

    @Override
    public void addCustomer(Customer customer) {
        CustomerEntity customerEntity = CustomerEntity.builder()
                .customerId(customer.getCustomerId())
                .name(customer.getName())
                .email(customer.getEmail())
                .privileges(customer.isPrivileges())
                .build();
        customRepository.save(customerEntity);
    }


    @Override
    public Optional<Customer> getCustomer(UUID id) {
        return customRepository.findById(id)
                .map(customerEntity -> new Customer(
                        customerEntity.getCustomerId(),
                        customerEntity.getName(),
                        customerEntity.getEmail(),
                        customerEntity.isPrivileges()
                ));
    }

    @Override
    public Optional<Customer> getCustomerByName(String name) {
        return customRepository.findByName(name)
                .map(customerEntity -> new Customer(
                        customerEntity.getCustomerId(),
                        customerEntity.getName(),
                        customerEntity.getEmail(),
                        customerEntity.isPrivileges()
                ));
    }

    @Override
    public void updatePrivileges(Customer customer) {
        customRepository.findById(customer.getCustomerId())
                .ifPresentOrElse(customerEntity -> {
                    customerEntity.setPrivileges(customer.isPrivileges());
                    customRepository.save(customerEntity);
                }, () -> {
                    throw new EntityNotFoundException("Customer with ID " + customer.getCustomerId() + " not found");
                });
    }
    @Override
    public void updateCustomer(Customer customer) {
        customRepository.findById(customer.getCustomerId())
                .ifPresentOrElse(customerEntity -> {
                    customerEntity.setName(customer.getName());
                    customerEntity.setEmail(customer.getEmail());
                    customerEntity.setPrivileges(customer.isPrivileges());
                    customRepository.save(customerEntity);
                }, () -> {
                    throw new EntityNotFoundException("Customer with ID " + customer.getCustomerId() + " not found");
                });
    }

    @Override
    public void deleteCustomer(UUID id) {
        // Delete customer by ID
        if (customRepository.existsById(id)) {
            customRepository.deleteById(id);
        } else {
            throw new RuntimeException("Customer with ID " + id + " not found!");
        }
    }
}
