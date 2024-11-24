package app.adapters.in;

import app.adapters.in.dto.CreateNewCustomer;
import app.domain.models.Customer;
import app.domain.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Customer> createNewCustomer(@RequestBody CreateNewCustomer newCustomer) {

        Customer customer = customerService.createNewCustomer(newCustomer);

        return ResponseEntity.ok(customer);
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> updateCustomer(@PathVariable UUID id, @RequestBody Customer customer) {
        customer.setCustomerId(id);
        customerService.updateCustomer(customer);
        return ResponseEntity.status(HttpStatus.OK).body("Customer updated successfully!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.status(HttpStatus.OK).body("Customer successfully deleted!");
    }
    @PutMapping("/{id}/privileges")
    public ResponseEntity<String> updateCustomerPrivileges(@PathVariable UUID id, @RequestBody boolean privileges) {
        // Fetch the existing customer
        Customer customer = customerService.findCustomerById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));

        // Update the privilege status
        customer.setPrivileges(privileges);
        customerService.updatePrivileges(customer);

        return ResponseEntity.ok("Customer privileges updated successfully!");
    }
}
