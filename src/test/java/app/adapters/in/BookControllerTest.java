package app.adapters.in;

import app.adapters.in.dto.CreateNewAuthor;
import app.adapters.out.MySQL.repositories.AuthorRepository;
import app.adapters.out.MySQL.repositories.BookRepository;
import app.domain.models.Author;
import app.domain.models.Book;
import app.domain.services.BookService;
import app.adapters.in.dto.CreateNewBook;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @BeforeEach
    public void setUp() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    public void testCreateNewBook() throws Exception {
        CreateNewBook newBook = new CreateNewBook("Test Book", "1234567890",
                2021, List.of(
                        new CreateNewAuthor("Test Author", "test")));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.isbn").value("1234567890"))
                .andExpect(jsonPath("$.publicationYear").value(2021))
                .andExpect(jsonPath("$.authors[0].name").value("Test Author"));

        assertEquals("Test Book", newBook.getTitle());
        assertEquals("1234567890", newBook.getIsbn());
        assertEquals(2021, newBook.getPublicationYear());
        assertEquals("Test Author", newBook.getAuthors().getFirst().getName());
        assertEquals(11, bookRepository.findAll().size());
    }

    @Test
    public void testGetAllBooks() throws Exception {
        mockMvc.perform(get("/books/paginated")
                        .param("page", "0")
                        .param("size", "3")
                        .param("sortBy", "title"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());

        assertEquals(10, bookRepository.findAll().size());
    }

    @Test
    public void testUpdateBook() throws Exception {
        Book createdBook = bookService.createNewBook(
                new CreateNewBook("Test Book", "1234567890",
                        2021, List.of(
                        new CreateNewAuthor("Test Author", "test"))));

        // Define the book update details
        Book bookToUpdate = new Book();
        bookToUpdate.setTitle("Updated Title");
        bookToUpdate.setIsbn("1234567890");
        bookToUpdate.setPublicationYear(2021);
        bookToUpdate.setAvailable(true);
        bookToUpdate.setCreatedAt(LocalDate.now());
        bookToUpdate.setAuthors(Set.of(new Author("Updated Author", "updated")));

        // Perform the PUT request to update the book
        mockMvc.perform(put("/books/" + createdBook.getBookId())  // Use the saved book's ID
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookToUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().string("Book updated successfully"));

        assertEquals("Updated Title", bookToUpdate.getTitle());
        assertEquals("1234567890", bookToUpdate.getIsbn());
        assertEquals(2021, bookToUpdate.getPublicationYear());
        assertTrue(bookToUpdate.isAvailable());
        assertNotNull(bookToUpdate.getCreatedAt());
    }

    @Test
    public void testDeleteBook() throws Exception {
        Book book = bookService.createNewBook(
                new CreateNewBook("Test Book", "1234567890",
                        2021, List.of(
                        new CreateNewAuthor("Test Author", "test"))));

        mockMvc.perform(delete("/books/" + book.getBookId()))
                .andExpect(status().isOk())
                .andExpect(content().string("Book successfully deleted!!"));

        assertEquals(10, bookRepository.findAll().size());

    }

    @Test
    public void testGetBookById() throws Exception {
        Book book = bookService.createNewBook(
                new CreateNewBook("Test Book", "1234567890",
                        2021, List.of(
                        new CreateNewAuthor("Test Author", "test"))));

        mockMvc.perform(get("/books")
                        .param("id", book.getBookId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.isbn").value("1234567890"))
                .andExpect(jsonPath("$.publicationYear").value(2021))
                .andExpect(jsonPath("$.authors[0].name").value("Test Author"));

    }
    @Test
    public void testGetBookById_NotFound() throws Exception {
        mockMvc.perform(get("/books")
                        .param("id", "12345678-1234-1234-1234-123456789012"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Book not found"));
    }
    @Test
    public void testGetBookByTitle() throws Exception {
        bookService.createNewBook(
                new CreateNewBook("Test Book", "1234567890",
                        2021, List.of(
                        new CreateNewAuthor("Test Author", "test"))));

        mockMvc.perform(get("/books")
                        .param("title", "Test Book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.isbn").value("1234567890"))
                .andExpect(jsonPath("$.publicationYear").value(2021))
                .andExpect(jsonPath("$.authors[0].name").value("Test Author"));
    }
    @Test
    public void testGetBookByTitle_NotFound() throws Exception {
        mockMvc.perform(get("/books")
                        .param("title", "Test Book"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Book with the given title not found"));
    }
    @Test
    public void testGetBookByIsbn() throws Exception {
        bookService.createNewBook(
                new CreateNewBook("Test Book", "1234567890",
                        2021, List.of(
                        new CreateNewAuthor("Test Author", "test"))));

        mockMvc.perform(get("/books")
                        .param("isbn", "1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.isbn").value("1234567890"))
                .andExpect(jsonPath("$.publicationYear").value(2021))
                .andExpect(jsonPath("$.authors[0].name").value("Test Author"));
    }
    @Test
    public void testGetBookByIsbn_NotFound() throws Exception {
        mockMvc.perform(get("/books")
                        .param("isbn", "1234567890"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Book with the given ISBN not found"));
    }
    @Test
    public void testGetBookByAuthor() throws Exception {
        bookService.createNewBook(
                new CreateNewBook("Test Book", "1234567890",
                        2021, List.of(
                                new CreateNewAuthor("Test Author", "test"))));

        mockMvc.perform(get("/books")
                        .param("author", "Test Author"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.isbn").value("1234567890"))
                .andExpect(jsonPath("$.publicationYear").value(2021))
                .andExpect(jsonPath("$.authors[0].name").value("Test Author"));
    }
    @Test
    public void testGetBookByAuthor_NotFound() throws Exception {
        mockMvc.perform(get("/books")
                        .param("author", "Test Author"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No books found by the given author"));
    }
    @Test
    public void testGetBookByQuery() throws Exception {
        bookService.createNewBook(
                new CreateNewBook("Test Book", "1234567890",
                        2021, List.of(
                        new CreateNewAuthor("Test Author", "test"))));

        mockMvc.perform(get("/books")
                        .param("query", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Book"))  // Access the first element of the array
                .andExpect(jsonPath("$[0].isbn").value("1234567890"))
                .andExpect(jsonPath("$[0].publicationYear").value(2021))
                .andExpect(jsonPath("$[0].authors[0].name").value("Test Author"));
    }
    @Test
    public void testGetBookByQuery_multipleResults() throws Exception {
        // First request for page 0
        mockMvc.perform(get("/books")
                        .param("query", "The")
                        .param("page", "0")  // First page
                        .param("size", "2"))  // Limit size to match the first page
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))  // Only 2 books in the first page
                .andExpect(jsonPath("$[0].title").value("The Catcher in the Rye"))
                .andExpect(jsonPath("$[1].title").value("The Divine Comedy"));

        // Second request for page 1
        mockMvc.perform(get("/books")
                        .param("query", "The")
                        .param("page", "1")  // Second page
                        .param("size", "2"))  // Limit size to match the second page
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))  // Two more books in the second page
                .andExpect(jsonPath("$[0].title").value("The Great Gatsby"))
                .andExpect(jsonPath("$[1].title").value("The Hobbit"));

        // Third request for the last page
        mockMvc.perform(get("/books")
                        .param("query", "The")
                        .param("page", "2")  // Third page
                        .param("size", "2"))  // Limit size to match the last page
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))  // One last book on the final page
                .andExpect(jsonPath("$[0].title").value("The Odyssey"));
    }

    @Test
    public void testGetBookByQuery_NotFound() throws Exception {
        mockMvc.perform(get("/books")
                        .param("query", "Test Book"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No books found for the given query"));
    }
    @Test
    public void testNoCriteriaProvided() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("No search criteria provided"));
    }
    @AfterEach
    public void tearDown() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }
}
