package app.domain.services;

import app.adapters.in.dto.CreateNewAuthor;
import app.adapters.in.dto.CreateNewBook;
import app.adapters.out.MySQL.repositories.AuthorRepository;
import app.adapters.out.MySQL.repositories.BookRepository;
import app.domain.models.Author;
import app.domain.models.Book;
import app.domain.port.BookDao;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookServiceTest {

    // Mocked Dependencies for Unit Tests
    private BookDao mockedBookDao;
    private AuthorService mockedAuthorService;

    // Real Dependencies for Integration Tests
    @Autowired
    private BookDao realBookDao;

    @Autowired
    private BookService realBookService;

    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private AuthorRepository authorRepository;

    @BeforeAll
    void setup() {
        mockedBookDao = mock(BookDao.class);
        mockedAuthorService = mock(AuthorService.class);
        bookService = new BookService(mockedBookDao, mockedAuthorService);
    }

    @Nested
    @DisplayName("Unit Tests")
    class UnitTests {

        @Test
        void createNewBook_Success() {
            CreateNewBook newBook = new CreateNewBook(
                    "Effective Java",
                    "123456789",
                    2018,
                    List.of(new CreateNewAuthor("Joshua Bloch", "example"))
            );

            when(mockedBookDao.searchBookByTitle("Effective Java")).thenReturn(Optional.empty());
            when(mockedBookDao.searchByIsbn("123456789")).thenReturn(Optional.empty());
            when(mockedAuthorService.getAuthorByName("Joshua Bloch")).thenReturn(Optional.empty());
            when(mockedAuthorService.createNewAuthor(any())).thenReturn(new Author("Joshua Bloch", "example"));

            bookService.createNewBook(newBook);

            ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
            verify(mockedBookDao).addBook(captor.capture());
            Book createdBook = captor.getValue();

            assertEquals("Effective Java", createdBook.getTitle());
            assertEquals("123456789", createdBook.getIsbn());
            assertEquals(2018, createdBook.getPublicationYear());
            assertTrue(createdBook.isAvailable());
            assertEquals(1, createdBook.getAuthors().size());
        }
        @Test
        void deleteBook_Success() {
            UUID bookId = UUID.randomUUID();

            doNothing().when(mockedBookDao).deleteBook(bookId);

            bookService.deleteBook(bookId);

            verify(mockedBookDao, times(1)).deleteBook(bookId);
        }
        @Test
        void searchBookByTitle_Found() {
            String title = "Sample Title";
            Book book = new Book(title, "ISBN1", 2021, true, null);

            when(mockedBookDao.searchBookByTitle(title)).thenReturn(Optional.of(book));

            Optional<Book> result = bookService.searchBookByTitle(title);

            assertTrue(result.isPresent());
            assertEquals(title, result.get().getTitle());
            verify(mockedBookDao, times(1)).searchBookByTitle(title);
        }
        @Test
        void searchBookByAuthors_Found() {
            String author = "John Doe";
            boolean isAvailable = true;
            Book book = new Book("Title", "ISBN1", 2021, isAvailable, null);

            when(mockedBookDao.searchBookByAuthors(author, isAvailable)).thenReturn(Optional.of(book));

            Optional<Book> result = bookService.searchBookByAuthors(author, isAvailable);

            assertTrue(result.isPresent());
            assertEquals("Title", result.get().getTitle());
            verify(mockedBookDao, times(1)).searchBookByAuthors(author, isAvailable);
        }

        @Test
        void searchByIsbn_Found() {
            String isbn = "123-456-789";
            Book book = new Book("Title", isbn, 2021, true, null);

            when(mockedBookDao.searchByIsbn(isbn)).thenReturn(Optional.of(book));

            Optional<Book> result = bookService.searchByIsbn(isbn);

            assertTrue(result.isPresent());
            assertEquals(isbn, result.get().getIsbn());
            verify(mockedBookDao, times(1)).searchByIsbn(isbn);
        }

        @Test
        void searchById_Found() {
            UUID id = UUID.randomUUID();
            Book book = new Book("Title", "ISBN", 2021, true, null);

            when(mockedBookDao.searchBookById(id)).thenReturn(Optional.of(book));

            Optional<Book> result = bookService.searchById(id);

            assertTrue(result.isPresent());
            assertEquals("Title", result.get().getTitle());
            verify(mockedBookDao, times(1)).searchBookById(id);
        }

        @Test
        void searchBooks_Found() {
            String query = "search query";
            Pageable pageable = PageRequest.of(0, 10);
            List<Book> books = Arrays.asList(new Book("Title1", "ISBN1", 2021, true, null),
                    new Book("Title2", "ISBN2", 2020, false, null));
            Page<Book> page = new PageImpl<>(books, pageable, books.size());

            when(mockedBookDao.searchBooks(query, pageable)).thenReturn(page);

            Page<Book> result = bookService.searchBooks(query, pageable);

            assertEquals(2, result.getTotalElements());
            verify(mockedBookDao, times(1)).searchBooks(query, pageable);
        }

        @Test
        void updateBook_Success() {
            UUID bookId = UUID.randomUUID();
            Book bookToUpdate = new Book("Updated Title", "111222333", 2021, true, LocalDate.now());

            bookService.updateBook(bookId, bookToUpdate);

            verify(mockedBookDao).updateBook(bookId, bookToUpdate);
        }

        @Test
        void getPaginatedBooks_ReturnsPaginatedResults() {
            PageRequest pageRequest = PageRequest.of(0, 10);
            Page<Book> paginatedBooks = new PageImpl<>(List.of(new Book("Title", "ISBN", 2020, true, LocalDate.now())));

            when(mockedBookDao.getPaginatedBooks(pageRequest)).thenReturn(paginatedBooks);

            Page<Book> result = bookService.getPaginatedBooks(pageRequest);

            assertEquals(1, result.getTotalElements());
            verify(mockedBookDao).getPaginatedBooks(pageRequest);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class IntegrationTests {

        @Test
        void createNewBook_IntegrationTest() {
            CreateNewBook newBook = new CreateNewBook(
                    "Spring Boot in Action",
                    "9876543210",
                    2021,
                    List.of(new CreateNewAuthor("Craig Walls", "example"))
            );

            Book createdBook = realBookService.createNewBook(newBook);

            assertNotNull(createdBook.getBookId());
            assertEquals("Spring Boot in Action", createdBook.getTitle());
            assertTrue(realBookDao.searchBookByTitle("Spring Boot in Action").isPresent());
        }
        @Test
        void updateBook_IntegrationTest() {
            // Arrange: Create and save an initial book in the database
            Book originalBook = new Book(
                    "Original Title", "123-456-789",
                    2021, true, LocalDate.now());
            realBookDao.addBook(originalBook);

            // Modify the book object for updating
            Book updatedBook = new Book("Updated Title", "123-456-789", 2022, false, LocalDate.now());
            UUID bookID = originalBook.getBookId();

            // Act: Call the service's updateBook method
            realBookService.updateBook(bookID, updatedBook);

            // Assert: Retrieve the updated book and verify changes
            Optional<Book> result = realBookDao.searchBookById(bookID);

            assertTrue(result.isPresent());
            assertEquals("Updated Title", result.get().getTitle());
            assertEquals(2022, result.get().getPublicationYear());
            assertFalse(result.get().isAvailable());
        }
        @Test
        void deleteBook_IntegrationTest() {
            Book book = new Book("Title", "ISBN", 2021,
                    true, LocalDate.now());
            realBookDao.addBook(book);

            realBookService.deleteBook(book.getBookId());

            Optional<Book> result = realBookDao.searchBookById(book.getBookId());
            assertTrue(result.isEmpty());
        }
        @Test
        void searchBookByTitle_IntegrationTest() {
            String title = "Unique Title";
            Book book = new Book(title, "ISBN", 2021, true,
                    LocalDate.now());

            realBookDao.addBook(book);

            Optional<Book> result = realBookService.searchBookByTitle(title);

            assertTrue(result.isPresent());
            assertEquals(title, result.get().getTitle());
        }
        @Test
        void searchByIsbn_IntegrationTest() {
            String isbn = "123-456-789";
            Book book = new Book("Title", isbn, 2021,
                    true, LocalDate.now());

            realBookDao.addBook(book);

            Optional<Book> result = realBookService.searchByIsbn(isbn);

            assertTrue(result.isPresent());
            assertEquals(isbn, result.get().getIsbn());
        }

        @Test
        void searchById_IntegrationTest() {
            Book book = new Book("Title", "ISBN", 2021,
                    true, LocalDate.now());
            realBookDao.addBook(book);

            Optional<Book> result = realBookService.searchById(book.getBookId());

            assertTrue(result.isPresent());
            assertEquals("Title", result.get().getTitle());
        }

        @Test
        void searchBooks_IntegrationTest() {
            String query = "search";
            Book book1 = new Book("Search Result 1", "ISBN1", 2021, true, LocalDate.now());
            Book book2 = new Book("Search Result 2", "ISBN2", 2020, false, LocalDate.now());

            realBookDao.addBook(book1);
            realBookDao.addBook(book2);

            Page<Book> result = realBookService.searchBooks(query, PageRequest.of(0, 10));

            assertEquals(2, result.getTotalElements());
        }

        @Test
        void getPaginatedBooks_IntegrationTest() {
            Pageable pageable = PageRequest.of(0, 10);
            Book book1 = new Book("Title1", "ISBN1", 2021, true, LocalDate.now());
            Book book2 = new Book("Title2", "ISBN2", 2020, false, LocalDate.now());

            realBookDao.addBook(book1);
            realBookDao.addBook(book2);

            Page<Book> result = realBookService.getPaginatedBooks(pageable);

            assertEquals(12, result.getTotalElements());
        }
    }
    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
        authorRepository.deleteAll();
    }
}
