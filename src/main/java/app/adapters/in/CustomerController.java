package app.adapters.in;

import app.adapters.in.dto.CreateNewCustomer;
import app.domain.models.Customer;
import app.domain.services.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }
    @PostMapping(produces = "application/single-book-response+json;version=1")
    public ResponseEntity<Customer> createNewCustomer(@Valid @RequestBody CreateNewCustomer newCustomer) {

        Customer customer = customerService.createNewCustomer(newCustomer);

        return ResponseEntity.ok(customer);
    }
    @GetMapping(value = "/getCustomerById/{customerId}", produces = "application/single-customer-response+json;version=1")
    public ResponseEntity<Customer> getCustomerById(@NotNull @PathVariable UUID customerId) {
        return customerService.findCustomerById(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    @GetMapping(value = "/getCustomerByName/{customerName}", produces = "application/customer-response+json;version=1")
    public ResponseEntity<Customer> getCustomerByName(@NotNull @PathVariable String customerName) {
        return customerService.findCustomerByName(customerName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    // TODO: NEED TO BE TESTET
    @GetMapping(value = "/paginated", produces = "application/paginated-customers-response+json;version=1")
    public ResponseEntity<Page<Customer>> getPaginatedCustomers(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam Optional<String> sortBy
    ) {
        PageRequest pageable = PageRequest.of(
                page.orElse(0),  // Default to page 0
                size.orElse(10), // Default to 10 items per page
                Sort.Direction.ASC,
                sortBy.orElse("name") // Default sort field
        );

        return ResponseEntity.ok(customerService.getPaginatedCustomers(pageable));
    }

    @PutMapping(value = "/updateCustomer/{id}", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<String> updateCustomer(@NotNull @PathVariable UUID id, @RequestBody Customer customer) {
        customer.setCustomerId(id);
        customerService.updateCustomer(customer);
        return ResponseEntity.status(HttpStatus.OK).body("Customer updated successfully!");
    }

    @PutMapping(value = "/{id}/privileges", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<String> updateCustomerPrivileges(@NotNull @PathVariable UUID id, @RequestBody boolean privileges) {
        customerService.updatePrivileges(id, privileges);
        return ResponseEntity.ok("Customer privileges updated successfully!");
    }


    @DeleteMapping("/deleteCustomer/{id}")
    public ResponseEntity<String> deleteCustomer(@NotNull @PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.status(HttpStatus.OK).body("Customer successfully deleted!");
    }
}
