package app.domain.services;

import app.adapters.out.MySQL.repositories.CustomerRepository;
import app.domain.models.Customer;
import app.domain.port.CustomerDao;
import app.adapters.in.dto.CreateNewCustomer;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {

    @Mock
    private CustomerDao customerDao;

    @InjectMocks
    private CustomerService customerService;

    @Nested
    class UnitTests {

        @Test
        void testCreateNewCustomer() {
            // Create a mock CreateNewCustomer DTO
            CreateNewCustomer createNewCustomer = new CreateNewCustomer("John Doe", "john.doe@example.com", true);

            // Mock customerDao behavior
            Customer expectedCustomer = new Customer(null, createNewCustomer.getName(), createNewCustomer.getEmail(), true);
            doNothing().when(customerDao).addCustomer(argThat(c ->
                    c.getName().equals(expectedCustomer.getName()) &&
                            c.getEmail().equals(expectedCustomer.getEmail()) &&
                            c.isPrivileges() == expectedCustomer.isPrivileges()));

            // Call the service method
            Customer actualCustomer = customerService.createNewCustomer(createNewCustomer);

            // Assert that the individual fields are equal
            assertThat(actualCustomer.getName()).isEqualTo(expectedCustomer.getName());
            assertThat(actualCustomer.getEmail()).isEqualTo(expectedCustomer.getEmail());
            assertThat(actualCustomer.isPrivileges()).isEqualTo(expectedCustomer.isPrivileges());

            // Verify that the DAO method was called with the correct argument
            verify(customerDao).addCustomer(argThat(c ->
                    c.getName().equals(expectedCustomer.getName()) &&
                            c.getEmail().equals(expectedCustomer.getEmail()) &&
                            c.isPrivileges() == expectedCustomer.isPrivileges()));
        }
        @Test
        void testFindCustomerById_Found() {
            // Create a mock customer
            UUID customerId = UUID.randomUUID();
            Customer expectedCustomer = new Customer(customerId, "Jane Doe", "jane.doe@example.com", true);

            // Mock customerDao behavior
            when(customerDao.getCustomer(customerId)).thenReturn(Optional.of(expectedCustomer));

            // Call the service method
            Optional<Customer> actualCustomer = customerService.findCustomerById(customerId);

            // Assert that the returned customer is as expected
            assertThat(actualCustomer).isPresent();
            assertThat(actualCustomer.get()).isEqualTo(expectedCustomer);
            verify(customerDao).getCustomer(customerId);
        }

        @Test
        void testFindCustomerById_NotFound() {
            // Create a random customer ID
            UUID customerId = UUID.randomUUID();

            // Mock customerDao behavior
            when(customerDao.getCustomer(customerId)).thenReturn(Optional.empty());

            // Call the service method
            Optional<Customer> actualCustomer = customerService.findCustomerById(customerId);

            // Assert that the returned Optional is empty
            assertThat(actualCustomer).isEmpty();
            verify(customerDao).getCustomer(customerId);
        }

        @Test
        void testFindCustomerByName_Found() {
            // Given
            String customerName = "John Doe";
            Customer expectedCustomer = new Customer(UUID.randomUUID(), customerName, "john.doe@example.com", true);
            when(customerDao.getCustomerByName(customerName)).thenReturn(Optional.of(expectedCustomer));

            // When
            Optional<Customer> actualCustomer = customerService.findCustomerByName(customerName);

            // Then
            assertThat(actualCustomer).isPresent();
            assertThat(actualCustomer.get()).isEqualTo(expectedCustomer);
            verify(customerDao).getCustomerByName(customerName);
        }

        @Test
        void testFindCustomerByName_NotFound() {
            // Given
            String customerName = "Non-existent Customer";
            when(customerDao.getCustomerByName(customerName)).thenReturn(Optional.empty());

            // When
            Optional<Customer> actualCustomer = customerService.findCustomerByName(customerName);

            // Then
            assertThat(actualCustomer).isEmpty();
            verify(customerDao).getCustomerByName(customerName);
        }

        @Test
        void testGetPaginatedCustomers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Customer> expectedPage = mock(Page.class);
            when(customerDao.getPaginatedCustomers(pageable)).thenReturn(expectedPage);

            // When
            Page<Customer> actualPage = customerService.getPaginatedCustomers(pageable);

            // Then
            assertThat(actualPage).isEqualTo(expectedPage);
            verify(customerDao).getPaginatedCustomers(pageable);
        }

        @Test
        void testSearchCustomer() {
            // Given
            String query = "John";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Customer> expectedPage = mock(Page.class);
            when(customerDao.searchCustomer(query, pageable)).thenReturn(expectedPage);

            // When
            Page<Customer> actualPage = customerService.searchCustomer(query, pageable);

            // Then
            assertThat(actualPage).isEqualTo(expectedPage);
            verify(customerDao).searchCustomer(query, pageable);
        }

        @Test
        void testUpdatePrivileges_CustomerFound() {
            // Given
            UUID customerId = UUID.randomUUID();
            Customer mockCustomer = mock(Customer.class); // Mock the Customer object
            when(customerDao.getCustomer(customerId)).thenReturn(Optional.of(mockCustomer));
            boolean newPrivileges = true;

            // When
            customerService.updatePrivileges(customerId, newPrivileges);

            // Then
            verify(customerDao).getCustomer(customerId);
            verify(mockCustomer).setPrivileges(newPrivileges); // Verify on the mock
            verify(customerDao).updatePrivileges(mockCustomer);
        }

        @Test
        void testUpdatePrivileges_CustomerNotFound() {
            // Given
            UUID customerId = UUID.randomUUID();
            when(customerDao.getCustomer(customerId)).thenReturn(Optional.empty());
            boolean newPrivileges = true;

            // When
            assertThrows(EntityNotFoundException.class, () -> customerService.updatePrivileges(customerId, newPrivileges));

            // Then
            verify(customerDao).getCustomer(customerId);
            verify(customerDao, never()).updatePrivileges(any(Customer.class));
        }

        @Test
        void testUpdateCustomer_CustomerFound() {
            // Given
            UUID customerId = UUID.randomUUID();
            Customer customer = new Customer(customerId, "John Doe", "john.doe@example.com", true);
            when(customerDao.getCustomer(customerId)).thenReturn(Optional.of(customer));

            // When
            customerService.updateCustomer(customer);

            // Then
            verify(customerDao).getCustomer(customerId);
            verify(customerDao).updateCustomer(customer);
        }

        @Test
        void testUpdateCustomer_CustomerNotFound() {
            // Given
            UUID customerId = UUID.randomUUID();
            Customer customer = new Customer(customerId, "John Doe", "john.doe@example.com", true);
            when(customerDao.getCustomer(customerId)).thenReturn(Optional.empty());

            // When
            assertThrows(EntityNotFoundException.class, () -> customerService.updateCustomer(customer));

            // Then
            verify(customerDao).getCustomer(customerId);
            verify(customerDao, never()).updateCustomer(customer);
        }

        @Test
        void testDeleteCustomer() {
            // Given
            UUID customerId = UUID.randomUUID();

            // When
            customerService.deleteCustomer(customerId);

            // Then
            verify(customerDao).deleteCustomer(customerId);
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    class IntegrationTests {

        @Autowired
        private CustomerService customerService;

        @Autowired
        private CustomerDao customerDao;

        @Autowired
        private CustomerRepository customerRepository;

        @Test
        void testCreateNewCustomer_PersistsToDatabase() {
            // Create a CreateNewCustomer DTO
            CreateNewCustomer createNewCustomer = new CreateNewCustomer("John Doe", "john.doe@example.com", true);

            // Call the service method
            customerService.createNewCustomer(createNewCustomer);

            // Retrieve the created customer
            Optional<Customer> createdCustomer = customerDao.getCustomerByName(createNewCustomer.getName());

            // Assert that the customer is persisted in the database
            assertThat(createdCustomer).isPresent();
            assertThat(createdCustomer.get().getName()).isEqualTo(createNewCustomer.getName());
            assertThat(createdCustomer.get().getEmail()).isEqualTo(createNewCustomer.getEmail());
        }

        @Test
        void testFindCustomerById_FromDatabase() {
            // Create a customer and persist it in the database
            Customer expectedCustomer = new Customer(null, "Jane Doe", "jane.doe@example.com", true);
            customerDao.addCustomer(expectedCustomer);

            // Call the service method
            Optional<Customer> actualCustomer = customerService.findCustomerById(expectedCustomer.getCustomerId());

            // Assert that the returned customer has the same content as expected
            assertThat(actualCustomer).isPresent();
            assertThat(actualCustomer.get()).usingRecursiveComparison().isEqualTo(expectedCustomer);
        }

        @Test
        void testFindCustomerByName_FromDatabase() {
            // Given
            String customerName = "John Doe";
            Customer expectedCustomer = new Customer(null, customerName, "john.doe@example.com", true);
            customerDao.addCustomer(expectedCustomer);

            // When
            Optional<Customer> actualCustomer = customerService.findCustomerByName(customerName);

            // Then
            assertThat(actualCustomer).isPresent();
            assertThat(actualCustomer.get()).usingRecursiveComparison().isEqualTo(expectedCustomer);
        }

        @Test
        void testGetPaginatedCustomers_FromDatabase() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            int numberOfCustomersToCreate = 20; // Create more customers than the page size

            // Create test data
            for (int i = 0; i < numberOfCustomersToCreate; i++) {
                customerDao.addCustomer(new Customer(null, "Customer " + i, "customer" + i + "@example.com", true));
            }

            // When
            Page<Customer> actualPage = customerService.getPaginatedCustomers(pageable);

            // Then
            assertThat(actualPage.getContent()).hasSize(10); // Assert that the page size is 10
            assertThat(actualPage.getTotalElements()).isEqualTo(numberOfCustomersToCreate); // Assert total number of elements
            assertThat(actualPage.getTotalPages()).isGreaterThanOrEqualTo(2); // Assert that there are multiple pages
        }

        @Test
        void testSearchCustomer_FromDatabase() {
            // Given
            String query = "John";
            Pageable pageable = PageRequest.of(0, 10);

            // Create test data
            customerDao.addCustomer(new Customer(null, "John Doe", "john.doe@example.com", true));
            customerDao.addCustomer(new Customer(null, "Jane Doe", "jane.doe@example.com", true));
            customerDao.addCustomer(new Customer(null, "John Smith", "john.smith@example.com", true));
            customerDao.addCustomer(new Customer(null, "David Lee", "david.lee@example.com", true));

            // When
            Page<Customer> actualPage = customerService.searchCustomer(query, pageable);

            // Then
            assertThat(actualPage.getContent()).hasSize(2); // Expecting 2 customers with "John" in their name
            assertThat(actualPage.getContent()).anyMatch(c -> c.getName().equals("John Doe"));
            assertThat(actualPage.getContent()).anyMatch(c -> c.getName().equals("John Smith"));
        }

        @Test
        void testUpdatePrivileges_FromDatabase() {
            // Given
            Customer customer = new Customer(null, "John Doe", "john.doe@example.com", false);
            customerDao.addCustomer(customer);
            boolean newPrivileges = true;

            // When
            customerService.updatePrivileges(customer.getCustomerId(), newPrivileges);

            // Then
            Optional<Customer> updatedCustomer = customerDao.getCustomer(customer.getCustomerId());
            assertThat(updatedCustomer).isPresent();
            assertThat(updatedCustomer.get().isPrivileges()).isTrue();
        }

        @Test
        void testUpdateCustomer_FromDatabase() {
            // Given
            Customer customer = new Customer(null, "John Doe", "john.doe@example.com", true);
            customerDao.addCustomer(customer);
            customer.setName("Jane Doe");

            // When
            customerService.updateCustomer(customer);

            // Then
            Optional<Customer> updatedCustomer = customerDao.getCustomer(customer.getCustomerId());
            assertThat(updatedCustomer).isPresent();
            assertThat(updatedCustomer.get().getName()).isEqualTo("Jane Doe");
        }

        @Test
        void testDeleteCustomer_FromDatabase() {
            // Given
            Customer customer = new Customer(null, "John Doe", "john.doe@example.com", true);
            customerDao.addCustomer(customer);

            // When
            customerService.deleteCustomer(customer.getCustomerId());

            // Then
            Optional<Customer> deletedCustomer = customerDao.getCustomer(customer.getCustomerId());
            assertThat(deletedCustomer).isEmpty();
        }
        @AfterEach
        void tearDown() {
            customerRepository.deleteAll(); // Clean up the database after each test
        }
    }
}