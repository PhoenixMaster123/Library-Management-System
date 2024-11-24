package app.domain.services;

import app.domain.port.BookDao;
import app.adapters.in.dto.CreateNewBook;
import app.domain.models.Book;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
public class BookService {

    private final BookDao bookDao;

    public BookService(BookDao bookDao) {
        this.bookDao = bookDao;
    }
    public Book createNewBook(CreateNewBook bookToCreate) {
        Book book = new Book(bookToCreate.getTitle(), bookToCreate.getIsbn(), bookToCreate.getPublicationYear(), true, LocalDate.now());
        bookDao.addBook(book);
        return book;
    }
    public void updateBook(Book book) {
        bookDao.updateBook(book);
    }

    public void deleteBook(Book book) {
        bookDao.deleteBook(book);
    }
    public void deleteBookByTitle(String title) {
        bookDao.deleteBookByTitle(title);
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
}
