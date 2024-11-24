package app.domain.services;

import app.adapters.in.dto.CreateNewBook;
import app.domain.models.Book;
import app.domain.port.BookDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookServiceTest {
    @Mock
    private BookDao bookDao;

    @InjectMocks
    private BookService bookService;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        closeable = MockitoAnnotations.openMocks(this);
    }
    @AfterEach
    void tearDown() throws Exception {
        // Close mocks to release resources
        closeable.close();
    }
    @Test
    void test_createNewBook() {
        // Arrange
        CreateNewBook createNewBook = new CreateNewBook("Sample Title", "123456789", 2023);

        // Act
        bookService.createNewBook(createNewBook);

        // Assert
        verify(bookDao, times(1)).addBook(any(Book.class));

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookDao).addBook(bookCaptor.capture());
        Book capturedBook = bookCaptor.getValue();

        assertNotNull(capturedBook.getBookId());
        assertEquals("Sample Title", capturedBook.getTitle());
        assertEquals("123456789", capturedBook.getIsbn());
        assertEquals(2023, capturedBook.getPublicationYear());
        assertTrue(capturedBook.isAvailable());
        assertEquals(LocalDate.now(), capturedBook.getCreatedAt());
    }

    @Test
    void updateBook() {

    }

    @Test
    void deleteBook() {
    }

    @Test
    void deleteBookByTitle() {
    }

    @Test
    void searchBookByTitle() {
    }

    @Test
    void searchBookByAuthors() {
    }

    @Test
    void searchByIsbn() {
    }
}