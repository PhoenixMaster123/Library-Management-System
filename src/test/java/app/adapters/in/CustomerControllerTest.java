package app.adapters.in;

import app.adapters.in.dto.CreateNewCustomer;
import app.adapters.out.MySQL.repositories.CustomerRepository;
import app.domain.models.Customer;
import app.domain.services.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerService customerService;
    @Test
    void testCreateNewCustomer() throws Exception {
        CreateNewCustomer newCustomer =
                new CreateNewCustomer("Test Customer", "test@example.com", true);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Customer"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.privileges").value(true));
    }

    @Test
    void testCreateNewCustomerWithInvalidEmail() throws Exception {
        CreateNewCustomer newCustomer =
                new CreateNewCustomer("Test Customer", "test", true);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isBadRequest());
    }
    @Test
    void createNewCustomer_ShouldReturnBadRequest_WhenInputIsInvalid() throws Exception {
        // Arrange
        CreateNewCustomer invalidCustomer = new CreateNewCustomer("Test Customer", "test", true);

        // Act & Assert
        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCustomer)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists()) // Adjust to match the actual structure
                .andExpect(jsonPath("$.email").value("Email should be valid")); // Adjust to match the actual structure
    }
    @Test
    void testCreateNewCustomerWithInvalidName() throws Exception {
        CreateNewCustomer newCustomer =
                new CreateNewCustomer("", "test@example.com", true);

        mockMvc.perform(post("/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateCustomer() throws Exception {
        Customer customer = customerService.createNewCustomer(
                new CreateNewCustomer(
                        "Test Customer", "test@example.com", true));

        Customer customerToUpdate = new Customer(
                "Update Customer", "update@example.com",
                false);



        mockMvc.perform(put("/customers/" + customer.getCustomerId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerToUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().string("Customer updated successfully!"));

        assertEquals("Update Customer", customerRepository.findById(customer.getCustomerId()).get().getName());
        assertEquals("update@example.com", customerRepository.findById(customer.getCustomerId()).get().getEmail());
        assertFalse(customerRepository.findById(customer.getCustomerId()).get().isPrivileges());
    }

    @Test
    void testUpdateCustomerPrivileges() throws Exception {
        Customer customer = customerService.createNewCustomer(
                new CreateNewCustomer(
                        "Test Customer", "@example.com", true));

        mockMvc.perform(put("/customers/" + customer.getCustomerId() + "/privileges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("false"))
                .andExpect(status().isOk())
                .andExpect(content().string("Customer privileges updated successfully!"));

        assertFalse(customerRepository.findById(customer.getCustomerId()).get().isPrivileges());
    }
    @Test
    void updateCustomerPrivileges_ShouldReturnBadRequest_WhenInputIsInvalid() throws Exception {
        // Arrange
        Customer customer = customerService.createNewCustomer(
                new CreateNewCustomer("Test Customer", "test@example.com", true));

        // Act & Assert
        mockMvc.perform(put("/customers/" + customer.getCustomerId() + "/privileges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")) // Correct JSON null
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Invalid privileges value"));
    }
    @Test
    void testDeleteCustomer() throws Exception {
        Customer customer = customerService.createNewCustomer(
                new CreateNewCustomer(
                        "Test Customer", "test@example.com", true));

        mockMvc.perform(delete("/customers/" + customer.getCustomerId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Customer successfully deleted!"));

        assertFalse(customerRepository.findById(customer.getCustomerId()).isPresent());
    }
    @AfterEach
    void tearDown() {
        customerRepository.deleteAll();
    }
}