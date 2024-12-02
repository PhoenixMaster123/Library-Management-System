package app.adapters.out.MySQL;

import app.adapters.out.MySQL.entity.AuthorEntity;
import app.adapters.out.MySQL.entity.BookEntity;
import app.adapters.out.MySQL.repositories.AuthorRepository;
import app.adapters.out.MySQL.repositories.BookRepository;
import app.domain.models.Author;
import app.domain.port.BookDao;
import app.domain.models.Book;
import app.infrastructure.exceptions.BookNotFoundException;
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
                .authors(authorEntities)
                .build();

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
            bookRepository.deleteById(bookID);
        } else {
            throw new BookNotFoundException("Book not found with ID: " + bookID);
        }
    }
    @Override
    public Optional<Book> searchBookByTitle(String title) {
        return bookRepository.findBookByTitle(title)
                .map(bookEntity -> new Book(
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
                ));
    }

    @Override
    public List<Book> searchBookByAuthors(String author, boolean isAvailable) {
        List<BookEntity> entities = bookRepository.findBooksByAuthor(author, isAvailable);
        return entities.stream()
                .map(e -> new Book(
                        e.getBookId(),
                        e.getTitle(),
                        e.getIsbn(),
                        e.getPublicationYear(),
                        e.isAvailability(),
                        e.getCreated_at(),
                        e.getAuthors() != null
                                ? e.getAuthors().stream()
                                .map(authorEntity -> new Author(
                                        authorEntity.getAuthorId(),
                                        authorEntity.getName(),
                                        authorEntity.getBio()
                                ))
                                .collect(Collectors.toSet())
                                : new HashSet<>()
                ))
                .toList();
    }

    @Override
    public Optional<Book> searchByIsbn(String isbn) {
        Optional<BookEntity> entity = bookRepository.findBooksByIsbn(isbn);
        return entity.map(e -> new Book(
                e.getBookId(),
                e.getTitle(),
                e.getIsbn(),
                e.getPublicationYear(),
                e.isAvailability(),
                e.getCreated_at(),
                e.getAuthors() != null
                        ? e.getAuthors().stream()
                        .map(authorEntity -> new Author(
                                authorEntity.getAuthorId(),
                                authorEntity.getName(),
                                authorEntity.getBio()
                        ))
                        .collect(Collectors.toSet())
                        : new HashSet<>()
        ));
    }

    @Override
    public Optional<Book> searchBookById(UUID id) {
        Optional<BookEntity> bookEntity = bookRepository.findBookByBookId(id);
        return bookEntity.map(e -> new Book(
                e.getBookId(),
                e.getTitle(),
                e.getIsbn(),
                e.getPublicationYear(),
                e.isAvailability(),
                e.getCreated_at(),
                e.getAuthors() != null
                        ? e.getAuthors().stream()
                        .map(authorEntity -> new Author(
                                authorEntity.getAuthorId(),
                                authorEntity.getName(),
                                authorEntity.getBio()
                        ))
                        .collect(Collectors.toSet())
                        : new HashSet<>()
        ));
    }
}
