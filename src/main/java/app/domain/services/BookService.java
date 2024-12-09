package app.domain.services;

import app.domain.models.Author;
import app.domain.port.BookDao;
import app.adapters.in.dto.CreateNewBook;
import app.domain.models.Book;
import jakarta.transaction.Transactional;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Transactional
public class BookService {

    private final BookDao bookDao;
    private final AuthorService authorService;
    private final CacheManager cacheManager;

    public BookService(BookDao bookDao, AuthorService authorService, CacheManager cacheManager) {
        this.bookDao = bookDao;
        this.authorService = authorService;
        this.cacheManager = cacheManager;
    }

    public Book createNewBook(CreateNewBook bookToCreate) {
        if (bookDao.searchBookByTitle(bookToCreate.getTitle()).isPresent()) {
            throw new IllegalArgumentException("Book with the same title already exists.");
        }
        if(bookDao.searchByIsbn(bookToCreate.getIsbn()).isPresent()) {
            throw new IllegalArgumentException("Book with the same isbn already exists.");
        }

        // Resolve authors (fetch existing or create new)
        Set<Author> authors = bookToCreate.getAuthors().stream()
                .map(authorDto -> authorService.getAuthorByName(authorDto.getName())
                        .orElseGet(() -> authorService.createNewAuthor(authorDto)))
                .collect(Collectors.toSet());

        // Create the book
        Book book = new Book(
                bookToCreate.getTitle(),
                bookToCreate.getIsbn(),
                bookToCreate.getPublicationYear(),
                true,
                LocalDate.now()
        );
        book.getAuthors().addAll(authors);

        bookDao.addBook(book);
        return book;
    }
    //@CachePut(value = "books", key = "#bookID") -> I need to return the book to use it
    public void updateBook(UUID bookID, Book book) {
        bookDao.updateBook(bookID, book);

        // Update the cache manually
        Cache cache = cacheManager.getCache("books");
        if (cache != null) {
            cache.put(bookID, book);
        }
    }

    @CacheEvict(value = "books", key = "#bookId")
    public void deleteBook(UUID bookId) {
        bookDao.deleteBook(bookId);
    }
    public Page<Book> getPaginatedBooks(Pageable pageable) {
        return bookDao.getPaginatedBooks(pageable);
    }
    @Cacheable(value = "books", key = "#title", unless = "#result == null")
    public Optional<Book> searchBookByTitle(String title) {
        return bookDao.searchBookByTitle(title);
    }

    public List<Book> searchBookByAuthors(String author, boolean isAvailable) {
        return bookDao.searchBookByAuthors(author, isAvailable);
    }

    @Cacheable(value = "books", key = "#isbn", unless = "#result == null")
    public Optional<Book> searchByIsbn(String isbn) {
        return bookDao.searchByIsbn(isbn);
    }
    @Cacheable(value = "books", key = "#id", unless = "#result == null")
    public Optional<Book> searchById(UUID id) {
        return bookDao.searchBookById(id);
    }
    public Page<Book> searchBooks(String query, Pageable pageable) {
        return bookDao.searchBooks(query, pageable);
    }
}
