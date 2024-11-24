package app.adapters.out.MySQL;

import app.adapters.out.MySQL.entity.BookEntity;
import app.adapters.out.MySQL.repositories.BookRepository;
import app.domain.port.BookDao;
import app.domain.models.Book;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BookDaoAdapter implements BookDao {
    private final BookRepository bookRepository;

    public BookDaoAdapter(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void addBook(Book book) {
        BookEntity bookEntity = BookEntity.builder()
                .bookId(book.getBookId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .publicationYear(book.getPublicationYear())
                .availability(book.isAvailable())
                .created_at(book.getCreatedAt())
                .build();
        bookRepository.save(bookEntity);
    }

    @Override
    public void updateBook(Book newBook) {
        bookRepository.findById(newBook.getBookId()).ifPresent(entity -> {
            entity.setTitle(newBook.getTitle());
            entity.setIsbn(newBook.getIsbn());
            entity.setPublicationYear(newBook.getPublicationYear());
            entity.setAvailability(newBook.isAvailable());
            entity.setCreated_at(newBook.getCreatedAt());
            bookRepository.save(entity);
        });
    }

    @Override
    public void deleteBook(Book book) {
        Optional<BookEntity> existingBook = bookRepository.findById(book.getBookId());
        if (existingBook.isPresent()) {
            bookRepository.deleteById(book.getBookId());
        } else {
            throw new RuntimeException("Book not found with ID: " + book.getBookId());
        }
    }
    @Override
    public void deleteBookByTitle(String title) {
        Optional<BookEntity> existingBook = bookRepository.findBookByTitle(title);
        if (existingBook.isPresent()) {
            bookRepository.deleteByTitle(title);
        } else {
            throw new RuntimeException("Book not found with Title: " + title);
        }
    }
    @Override
    public Optional<Book> searchBookByTitle(String title) {
        Optional<BookEntity> entity = bookRepository.findBookByTitle(title);
        return entity.map(e -> new Book(
                e.getTitle(),
                e.getIsbn(),
                e.getPublicationYear(),
                e.isAvailability(),
                e.getCreated_at()
        ));
    }

    @Override
    public List<Book> searchBookByAuthors(String author, boolean isAvailable) {
        List<BookEntity> entities = bookRepository.findBooksByAuthor(author, isAvailable);
        return entities.stream()
                .map(e -> new Book(
                        e.getTitle(),
                        e.getIsbn(),
                        e.getPublicationYear(),
                        e.isAvailability(),
                        e.getCreated_at()
                ))
                .toList();
    }

    @Override
    public Optional<Book> searchByIsbn(String isbn) {
        Optional<BookEntity> entity = bookRepository.findBooksByIsbn(isbn);
        return entity.map(e -> new Book(
                e.getTitle(),
                e.getIsbn(),
                e.getPublicationYear(),
                e.isAvailability(),
                e.getCreated_at()
        ));
    }
}
