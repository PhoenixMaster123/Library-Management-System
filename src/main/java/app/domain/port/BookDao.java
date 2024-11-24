package app.domain.port;

import app.domain.models.Book;

import java.util.List;
import java.util.Optional;

public interface BookDao {
    void addBook(Book book);
    void updateBook(Book book);
    void deleteBook(Book book);//or with ID
    void deleteBookByTitle(String title);
    Optional<Book> searchBookByTitle(String title);
    List<Book> searchBookByAuthors(String author, boolean isAvailable);
    Optional<Book> searchByIsbn(String isbn);

}
