package app.adapters.out.MySQL;

import app.adapters.out.MySQL.entity.AuthorEntity;
import app.adapters.out.MySQL.entity.BookEntity;
import app.adapters.out.MySQL.repositories.AuthorRepository;
import app.adapters.out.MySQL.repositories.BookRepository;
import app.domain.models.Author;
import app.domain.port.BookDao;
import app.domain.models.Book;
import app.infrastructure.exceptions.BookNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class BookDaoAdapter implements BookDao {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookDaoAdapter(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    @Override
    public void addBook(Book book) {
        // Check if authors exist in the DB, or create them
        Set<AuthorEntity> authorEntities = book.getAuthors().stream()
                .map(author -> {
                    Optional<AuthorEntity> existingAuthor = authorRepository.findByName(author.getName());
                    return existingAuthor.orElseGet(() -> {
                        AuthorEntity newAuthorEntity = new AuthorEntity(
                                UUID.randomUUID(),
                                author.getName(),
                                author.getBio(),
                                new HashSet<>()
                        );
                        return authorRepository.save(newAuthorEntity);
                    });
                })
                .collect(Collectors.toSet());

        // Persist the book with associated authors
        BookEntity bookEntity = BookEntity.builder()
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .publicationYear(book.getPublicationYear())
                .availability(book.isAvailable())
                .created_at(book.getCreatedAt())
                .build();

        authorEntities.forEach(author -> {
            if (author.getBooks() == null) {
                author.setBooks(new HashSet<>()); // Initialize books if it's null
            }
            author.getBooks().add(bookEntity); // Add the book to the author's books
        });
        // Save the entity and fetch the generated ID
        BookEntity savedEntity = bookRepository.save(bookEntity);
        book.setBookId(savedEntity.getBookId()); // Set the generated ID in the domain model
    }

    @Override
    public void updateBook(UUID bookID, Book newBook) {
        bookRepository.findById(bookID).ifPresent(entity -> {
            entity.setTitle(newBook.getTitle());
            entity.setIsbn(newBook.getIsbn());
            entity.setPublicationYear(newBook.getPublicationYear());
            entity.setAvailability(newBook.isAvailable());
            entity.setCreated_at(newBook.getCreatedAt());
            bookRepository.save(entity);
        });
    }

    @Override
    public void deleteBook(UUID bookID) {
        Optional<BookEntity> existingBook = bookRepository.findById(bookID);
        if (existingBook.isPresent()) {
            BookEntity book = existingBook.get();

            // Remove the association between the book and authors
            for (AuthorEntity author : book.getAuthors()) {
                author.getBooks().remove(book);  // Assuming you have a 'books' field in the AuthorEntity
            }

            // Clear the authors' reference in the book entity (optional, depending on your use case)
            book.setAuthors(new HashSet<>());

            // Save the updated book (this step might not be necessary depending on your setup)
            bookRepository.save(book);

            // Now delete the book itself
            bookRepository.deleteById(bookID);
        } else {
            throw new BookNotFoundException("Book not found with ID: " + bookID);
        }
    }

    @Override
    public Page<Book> getPaginatedBooks(Pageable pageable) {
        return bookRepository.findAll(pageable).map(this::mapToBook);
    }

    @Override
    public Optional<Book> searchBookByTitle(String title) {
        Optional<BookEntity> bookEntity = bookRepository.findBookByTitle(title);
        return bookEntity.map(this::mapToBook);
    }

    @Override
    public List<Book> searchBookByAuthors(String author, boolean isAvailable) {
        List<BookEntity> entities = bookRepository.findBooksByAuthor(author, isAvailable);
        return entities.stream()
                .map(this::mapToBook)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Book> searchByIsbn(String isbn) {
        Optional<BookEntity> bookEntity = bookRepository.findBooksByIsbn(isbn);
        return bookEntity.map(this::mapToBook);
    }

    @Override
    public Optional<Book> searchBookById(UUID id) {
        Optional<BookEntity> bookEntity = bookRepository.findBookByBookId(id);
        return bookEntity.map(this::mapToBook);
    }
    @Override
    public Page<Book> searchBooks(String query, Pageable pageable) {
        String lowerQuery = query.toLowerCase();
        // Filter and paginate using a repository query
        Page<BookEntity> bookEntities = bookRepository.findBooksByQuery(lowerQuery, pageable);

        return bookEntities.map(this::mapToBook);
    }
    private Book mapToBook(BookEntity bookEntity) {
        return new Book(
                bookEntity.getBookId(),
                bookEntity.getTitle(),
                bookEntity.getIsbn(),
                bookEntity.getPublicationYear(),
                bookEntity.isAvailability(),
                bookEntity.getCreated_at(),
                bookEntity.getAuthors() != null
                        ? bookEntity.getAuthors().stream()
                        .map(authorEntity -> new Author(
                                authorEntity.getAuthorId(),
                                authorEntity.getName(),
                                authorEntity.getBio()
                        ))
                        .collect(Collectors.toSet())
                        : new HashSet<>()
        );
    }

}
