package app.adapters.in;

import app.adapters.in.dto.CreateNewAuthor;
import app.adapters.in.dto.CreateNewBook;
import app.adapters.in.dto.CreateNewCustomer;
import app.adapters.in.dto.CreateNewTransaktion;
import app.adapters.out.MySQL.repositories.AuthorRepository;
import app.adapters.out.MySQL.repositories.BookRepository;
import app.adapters.out.MySQL.repositories.CustomerRepository;
import app.adapters.out.MySQL.repositories.TransactionRepository;
import app.domain.models.Book;
import app.domain.models.Customer;
import app.domain.models.Transaction;
import app.domain.services.BookService;
import app.domain.services.CustomerService;
import app.domain.services.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private BookService bookService;
    @Autowired
    private CustomerService customerService;
    private Customer customer;
    private Book book;
    @BeforeEach
    public void setUp() {
        customer = customerService.createNewCustomer(
                new CreateNewCustomer("Test Customer Transaction", "test_transaction@example.com", true));
        book = bookService.createNewBook(new CreateNewBook("Test Book Transaction", "1234567892",
                2021, List.of(
                new CreateNewAuthor("Test Author Transaction", "test"))));
    }
    @Test
    void testCreateNewTransaction() throws Exception {
        // Generate valid dates dynamically
        LocalDate today = LocalDate.now();
        LocalDate futureDueDate = today.plusDays(1); // Due date must be in the future

        UUID customerId = customer.getCustomerId();
        UUID bookId = book.getBookId();

        CreateNewTransaktion newTransaktion = new CreateNewTransaktion(
                today,              // borrowDate: today (valid)
                futureDueDate,      // dueDate: 1 day in the future (valid)
                customerId,
                bookId
        );
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTransaktion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.borrowDate").value(today.toString()))
                .andExpect(jsonPath("$.dueDate").value(futureDueDate.toString()))
                .andExpect(jsonPath("$.customer.customerId").value(newTransaktion.getCustomerId().toString()))
                .andExpect(jsonPath("$.book.bookId").value(newTransaktion.getBookId().toString()));
    }
    @Test
    void testBorrowBook() throws Exception {
        UUID customerId = customer.getCustomerId();
        UUID bookId = book.getBookId();

        mockMvc.perform(post("/transactions/borrowBook/{customerId}/{bookId}", customerId, bookId))
                .andExpect(status().isOk())
                .andExpect(content().string("Book borrowed successfully."));

        long transactionCount = transactionRepository.countByCustomer_CustomerId(customerId);
        assertEquals(1, transactionCount);
    }
    @Test
    void testBorrowBook_bookNotAvailable() throws Exception {
        UUID customerId = customer.getCustomerId();
        UUID bookId = book.getBookId();

        // Borrow the book
        transactionService.borrowBook(customerId, bookId);

        mockMvc.perform(post("/transactions/borrowBook/{customerId}/{bookId}", customerId, bookId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to borrow book: Book is not available for borrowing."));
    }
    @Test
    void testReturnBook() throws Exception {
        UUID customerId = customer.getCustomerId();
        UUID bookId = book.getBookId();

        // Borrow the book
        transactionService.borrowBook(customerId, bookId);

        mockMvc.perform(post("/transactions/returnBook/{bookId}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction successful."));
    }
    @Test
    void testReturnBook_noTransactionFound() throws Exception {
        UUID bookId = book.getBookId();

        mockMvc.perform(post("/transactions/returnBook/{bookId}", bookId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Failed to return book: No transaction found for the given book."));
    }
    @Test
    void testReturnBook_bookNotFound() throws Exception {
        UUID bookId = UUID.randomUUID();

        mockMvc.perform(post("/transactions/returnBook/{bookId}", bookId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found."));
    }
    @Test
    void testGetTransactionID() throws Exception {
        UUID customerId = customer.getCustomerId();
        UUID bookId = book.getBookId();

        // Borrow the book and create the transaction
        Transaction transaction = transactionService.borrowBook(customerId, bookId);

        // Ensure the transaction was saved
        assertNotNull(transaction);
        assertNotNull(transaction.getTransactionId());

        // Get the transaction ID after borrowing the book
        UUID transactionId = transaction.getTransactionId();


        // Perform the GET request to retrieve the transaction by ID
        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId.toString()))
                .andExpect(jsonPath("$.borrowDate").exists())
                .andExpect(jsonPath("$.dueDate").exists())
                .andExpect(jsonPath("$.customer.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.book.bookId").value(bookId.toString()));
    }
    @Test
    void testGetTransactionID_notFound() throws Exception {
        UUID transactionId = UUID.randomUUID();

        mockMvc.perform(get("/transactions/{id}", transactionId))
                .andExpect(status().isNotFound());}
    @Test
    void testViewBorrowingHistory() throws Exception {
        UUID customerId = customer.getCustomerId();
        UUID bookId = book.getBookId();

        // Borrow the book
        transactionService.borrowBook(customerId, bookId);

        // Perform the GET request to retrieve the borrowing history
        mockMvc.perform(get("/transactions/history/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].transactionId").exists())
                .andExpect(jsonPath("$.data[0].borrowDate").exists())
                .andExpect(jsonPath("$.data[0].dueDate").exists())
                .andExpect(jsonPath("$.data[0].customerId").value(customerId.toString()))  // Access customerId directly
                .andExpect(jsonPath("$.data[0].bookId").value(bookId.toString()))  // Access bookId directly
                .andExpect(jsonPath("$.data[0].book.bookId").value(bookId.toString()));
    }
    @Test
    void testViewBorrowingHistory_noTransactionsFound() throws Exception {
        UUID customerId = customer.getCustomerId();

        mockMvc.perform(get("/transactions/history/{customerId}", customerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No transactions found for this customer"));
    }
    @AfterEach
    public void tearDown() {
        transactionRepository.deleteAll();
        customerRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }
}