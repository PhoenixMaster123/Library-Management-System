package app.domain.port;

import app.domain.models.Book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookDao {
    void addBook(Book book);
    void updateBook(UUID bookID, Book book);
    void deleteBook(UUID bookId);
    Optional<Book> searchBookByTitle(String title);
    List<Book> searchBookByAuthors(String author, boolean isAvailable);
    Optional<Book> searchByIsbn(String isbn);
    Optional<Book> searchBookById(UUID id);

}
