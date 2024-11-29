package app.domain.services;

import app.domain.port.BookDao;
import app.adapters.in.dto.CreateNewBook;
import app.domain.models.Book;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class BookService {

    private final BookDao bookDao;

    public BookService(BookDao bookDao) {
        this.bookDao = bookDao;
    }
    public Book createNewBook(CreateNewBook bookToCreate) {
        if (bookDao.searchBookByTitle(bookToCreate.getTitle()).isPresent()) {
            throw new IllegalArgumentException("Book with the same title already exists.");
        }
        Book book = new Book(bookToCreate.getTitle(), bookToCreate.getIsbn(), bookToCreate.getPublicationYear(), true, LocalDate.now());
        bookDao.addBook(book);
        return book;
    }
    public void updateBook(UUID bookID, Book book) {
        bookDao.updateBook(bookID, book);
    }

    public void deleteBook(UUID bookId) {
        bookDao.deleteBook(bookId);
    }
    public Optional<Book> searchBookByTitle(String title) {
        return bookDao.searchBookByTitle(title);
    }

    public List<Book> searchBookByAuthors(String author, boolean isAvailable) {
        return bookDao.searchBookByAuthors(author, isAvailable);
    }

    public Optional<Book> searchByIsbn(String isbn) {
        return bookDao.searchByIsbn(isbn);
    }
    public Optional<Book> searchById(UUID id) {
        return bookDao.searchBookById(id);
    }
}
