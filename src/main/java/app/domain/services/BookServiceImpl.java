package app.domain.services;

import app.persistence.repository.BookRepository;
import app.persistence.entity.Book;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class BookServiceImpl {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book addOrUpdateBook(Book book) {
        return bookRepository.save(book);
    }

    public List<Book> searchBooks(String title, String author, String isbn) {
        return bookRepository.findAllByMultipleCriteria(title, author, isbn);
    }
}
