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
    @GetMapping(value = "/getCustomerById/{customerId}", produces = "application/single-customer-response+json;version=1")
    public ResponseEntity<Customer> getCustomerById(@PathVariable UUID customerId) {
        return customerService.findCustomerById(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping(value = "/getCustomerByName/{customerName}", produces = "application/customer-response+json;version=1")
    public ResponseEntity<Customer> getCustomerByName(@PathVariable String customerName) {
        return customerService.findCustomerByName(customerName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping(value = "/updateCustomer/{id}", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<String> updateCustomer(@PathVariable UUID id, @RequestBody Customer customer) {
        customer.setCustomerId(id);
        customerService.updateCustomer(customer);
        return ResponseEntity.status(HttpStatus.OK).body("Customer updated successfully!");
    }

    @PutMapping(value = "/{id}/privileges", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<String> updateCustomerPrivileges(@PathVariable UUID id, @RequestBody boolean privileges) {
        // Update privileges via the service layer
        customerService.updatePrivileges(id, privileges);
        return ResponseEntity.ok("Customer privileges updated successfully!");
    }


    @DeleteMapping("/deleteCustomer/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.status(HttpStatus.OK).body("Customer successfully deleted!");
    }
}
