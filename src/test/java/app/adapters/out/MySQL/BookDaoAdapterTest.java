package app.adapters.out.MySQL;

import app.domain.models.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import app.adapters.out.MySQL.entity.AuthorEntity;
import app.adapters.out.MySQL.entity.BookEntity;
import app.adapters.out.MySQL.repositories.AuthorRepository;
import app.adapters.out.MySQL.repositories.BookRepository;

import app.domain.models.Book;
import app.infrastructure.exceptions.BookNotFoundException;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookDaoAdapterTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorRepository authorRepository;
    @InjectMocks
    BookDaoAdapter dao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
        dao = new BookDaoAdapter(bookRepository, authorRepository); // Inject mocks manually
    }
    @Test
    void test_addBook_Success() {
        // Arrange
        Book book = new Book(UUID.randomUUID(), "Book Test", "9876543210", 2022, true, LocalDate.now());
        book.setAuthors(new HashSet<>(Arrays.asList(
                new Author("New Author 1", "Bio 1"),
                new Author("New Author 2", "Bio 2")
        )));

        AuthorEntity author1 = new AuthorEntity(UUID.randomUUID(), "New Author 1", "Bio 1", new HashSet<>());
        AuthorEntity author2 = new AuthorEntity(UUID.randomUUID(), "New Author 2", "Bio 2", new HashSet<>());

        when(authorRepository.findByName("New Author 1")).thenReturn(Optional.empty());  // Mock missing author
        when(authorRepository.findByName("New Author 2")).thenReturn(Optional.empty());  // Mock missing author
        when(authorRepository.save(any(AuthorEntity.class))).thenReturn(author1, author2);  // Mock saving authors
        when(bookRepository.save(any(BookEntity.class))).thenReturn(new BookEntity());  // Mock saving the book

        // Act
        dao.addBook(book);

        // Assert
        verify(authorRepository, times(2)).save(any(AuthorEntity.class));  // Verify authors were saved
        verify(bookRepository).save(any(BookEntity.class));  // Verify the book was saved
    }
    @Test
    public void testUpdateBook_updatesExistingBook() {
        // Mock data
        UUID bookId = UUID.randomUUID();
        Book updatedBook = new Book("Updated Title", "9876543210", 2024, false, LocalDate.now());
        BookEntity existingBookEntity = new BookEntity();
        existingBookEntity.setBookId(bookId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBookEntity));

        // Call the method
        BookDaoAdapter dao = new BookDaoAdapter(bookRepository, authorRepository);
        dao.updateBook(bookId, updatedBook);

        // Verify interactions
        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(existingBookEntity);
        assertEquals(existingBookEntity.getTitle(), updatedBook.getTitle());
        assertEquals(existingBookEntity.getIsbn(), updatedBook.getIsbn());
        assertEquals(existingBookEntity.getPublicationYear(), updatedBook.getPublicationYear());
        assertEquals(existingBookEntity.isAvailability(), updatedBook.isAvailable());
    }

    @Test
    public void testUpdateBook_doesNothingIfBookNotFound() {
        // Mock data
        UUID bookId = UUID.randomUUID();
        Book updatedBook = new Book("Updated Title", "9876543210", 2024, false, LocalDate.now());
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Call the method
        BookDaoAdapter dao = new BookDaoAdapter(bookRepository, authorRepository);
        dao.updateBook(bookId, updatedBook);

        // Verify interactions
        verify(bookRepository).findById(bookId);
        verify(bookRepository, never()).save(any(BookEntity.class));
    }

    @Test
    public void testDeleteBook_deletesBookAndRemovesAssociations() {
        // Mock data
        UUID bookId = UUID.randomUUID();
        BookEntity existingBookEntity = new BookEntity();
        existingBookEntity.setBookId(bookId);
        AuthorEntity authorEntity = new AuthorEntity();
        authorEntity.getBooks().add(existingBookEntity);
        existingBookEntity.setAuthors(Set.of(authorEntity));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBookEntity));

        // Call the method
        BookDaoAdapter dao = new BookDaoAdapter(bookRepository, authorRepository);
        dao.deleteBook(bookId);

        // Verify interactions
        verify(bookRepository).findById(bookId);
        verify(bookRepository).deleteById(bookId);
        assertTrue(authorEntity.getBooks().isEmpty());
    }

    @Test
    public void testDeleteBook_throwsExceptionIfBookNotFound() {
        // Mock data
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Call the method and assert exception
        BookDaoAdapter dao = new BookDaoAdapter(bookRepository, authorRepository);
        assertThrows(BookNotFoundException.class, () -> dao.deleteBook(bookId));
    }

    @Test
    void test_getPaginatedBooks() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);  // Example pageable with page 0, size 10
        BookEntity bookEntity = new BookEntity(UUID.randomUUID(), "Book 1", "1234567890", 2022, true, LocalDate.now(), new HashSet<>(), new ArrayList<>());
        Page<BookEntity> page = new PageImpl<>(Collections.singletonList(bookEntity));

        when(bookRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Book> result = dao.getPaginatedBooks(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Book 1", result.getContent().getFirst().getTitle());
    }
    @Test
    void test_searchBookByTitle() {
        // Arrange
        String title = "Book Test";
        BookEntity bookEntity = new BookEntity(UUID.randomUUID(), title, "9876543210", 2022, true, LocalDate.now(), new HashSet<>(), new ArrayList<>());
        when(bookRepository.findBookByTitle(title)).thenReturn(Optional.of(bookEntity));

        // Act
        Optional<Book> result = dao.searchBookByTitle(title);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(title, result.get().getTitle());
    }
    @Test
    void test_searchBookByAuthors() {
        // Arrange
        String authorName = "Author Test";
        boolean isAvailable = true;

        // Create an author and add it to the book
        AuthorEntity authorEntity = new AuthorEntity(UUID.randomUUID(), authorName, "Bio of Author", new HashSet<>());
        Set<AuthorEntity> authors = new HashSet<>();
        authors.add(authorEntity);

        // Create a book with the author and availability status
        BookEntity bookEntity = new BookEntity(UUID.randomUUID(), "Book Test", "9876543210", 2022, isAvailable, LocalDate.now(), authors, new ArrayList<>());

        // Mock the repository call to return the book with the author
        when(bookRepository.findBooksByAuthor(authorName, isAvailable)).thenReturn(Collections.singletonList(bookEntity));

        // Act
        Optional<Book> result = dao.searchBookByAuthors(authorName, isAvailable);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(authorName, result.get().getAuthors().iterator().next().getName());
    }
    @Test
    void test_searchByIsbn() {
        // Arrange
        String isbn = "9876543210";
        BookEntity bookEntity = new BookEntity(UUID.randomUUID(), "Book Test", isbn, 2022, true, LocalDate.now(), new HashSet<>(), new ArrayList<>());
        when(bookRepository.findBooksByIsbn(isbn)).thenReturn(Optional.of(bookEntity));

        // Act
        Optional<Book> result = dao.searchByIsbn(isbn);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(isbn, result.get().getIsbn());
    }
    @Test
    void test_searchBookById() {
        // Arrange
        UUID bookId = UUID.randomUUID();
        BookEntity bookEntity = new BookEntity(bookId, "Book Test", "9876543210", 2022, true, LocalDate.now(), new HashSet<>(), new ArrayList<>());
        when(bookRepository.findBookByBookId(bookId)).thenReturn(Optional.of(bookEntity));

        // Act
        Optional<Book> result = dao.searchBookById(bookId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(bookId, result.get().getBookId());
    }
    @Test
    void test_searchBooks() {
        // Arrange
        String query = "Test";
        Pageable pageable = PageRequest.of(0, 10);
        BookEntity bookEntity = new BookEntity(UUID.randomUUID(), "Book Test", "9876543210", 2022, true, LocalDate.now(), new HashSet<>(), new ArrayList<>());
        Page<BookEntity> page = new PageImpl<>(Collections.singletonList(bookEntity));

        when(bookRepository.findBooksByQuery(query.toLowerCase(), pageable)).thenReturn(page);

        // Act
        Page<Book> result = dao.searchBooks(query, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Book Test", result.getContent().getFirst().getTitle());
    }





}