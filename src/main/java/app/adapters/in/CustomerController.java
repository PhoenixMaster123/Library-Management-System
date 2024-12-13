package app.adapters.in;

import app.adapters.in.dto.CreateNewCustomer;
import app.domain.models.Customer;
import app.domain.services.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

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
    @GetMapping(value = "/search", produces = "application/single-customer-response+json;version=1")
    public ResponseEntity<Customer> getCustomer(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String query) {
        Optional<Customer> customer;

        if (customerId != null) {
            customer = customerService.findCustomerById(customerId);
        } else if (customerName != null && !customerName.isBlank()) {
            customer = customerService.findCustomerByName(customerName);
        } else if (query != null && !query.isBlank()) {
            customer = customerService.searchCustomer(query);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping(value = "/paginated", produces = "application/paginated-customers-response+json;version=1")
    public ResponseEntity<Map<String, Object>> getAllCustomers(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam Optional<String> sortBy
    ) {
        int currentPage = page.orElse(0);
        int pageSize = size.orElse(1);
        String sortField = sortBy.orElse("name");

        PageRequest pageable = PageRequest.of(currentPage, pageSize, Sort.Direction.ASC, sortField);
        Page<Customer> customers = customerService.getPaginatedCustomers(pageable);

        if (customers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No customers found"));
        }

        // Use LinkedHashMap to ensure field order in the response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalItems", customers.getTotalElements());
        response.put("data", customers.getContent());
        response.put("totalPages", customers.getTotalPages());
        response.put("currentPage", customers.getNumber());

        // Add pagination links at the end of the response
        response.put("self", generatePaginatedCustomerLink(currentPage, pageSize, sortField));
        if (customers.hasPrevious()) {
            response.put("prev", generatePaginatedCustomerLink(currentPage - 1, pageSize, sortField));
        }
        if (customers.hasNext()) {
            response.put("next", generatePaginatedCustomerLink(currentPage + 1, pageSize, sortField));
        }

        return ResponseEntity.ok(response);
    }

    private String generatePaginatedCustomerLink(int page, int size, String sortBy) {
        return linkTo(CustomerController.class).slash("paginated").toUriComponentsBuilder()
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortBy", sortBy)
                .toUriString();
    }

    @PutMapping(value = "/{id}", produces = "application/single-book-response+json;version=1")
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


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCustomer(@NotNull @PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.status(HttpStatus.OK).body("Customer successfully deleted!");
    }
}
