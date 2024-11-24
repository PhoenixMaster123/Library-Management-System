package app.adapters.in;

import app.adapters.in.dto.CreateNewBook;
import app.domain.models.Book;
import app.domain.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/books")
public class BookController {
    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping(produces = "application/single-book-response+json;version=1")
    public ResponseEntity<Book> createNewBook(@RequestBody CreateNewBook newBook) {

        Book book = bookService.createNewBook(newBook);

        return ResponseEntity.ok(book);
    }
    @GetMapping("/title/{title}")
    public ResponseEntity<?> getBookByTitle(@PathVariable("title") String title) {
        return ResponseEntity.ok(bookService.searchBookByTitle(title));
    }
    @GetMapping("/author/{author}")
    public ResponseEntity<?> getBookByAuthor(@PathVariable("author") String author) {
        return ResponseEntity.ok(bookService.searchBookByAuthors(author, true));
    }
    @GetMapping("/{isbn}")
    public ResponseEntity<?> getBookByIsbn(@PathVariable("isbn") String isbn) {
        return ResponseEntity.ok(bookService.searchByIsbn(isbn));
    }
    // TODO: It is not working: (I don't know how to test it)
    @PutMapping("/updateBook")
    public ResponseEntity<String> updateBook(@RequestBody Book book){
        Optional<Book> existingBook = bookService.searchByIsbn(book.getIsbn());
        if (existingBook.isEmpty()) {
            return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
        }
        bookService.updateBook(book);
        return new ResponseEntity<>("Book updated", HttpStatus.OK);
    }
    // TODO: It is not working: (I don't know how to test it)
    @DeleteMapping("/deleteBook")
    public ResponseEntity<String> deleteBook(@RequestParam("book") Book book){
        bookService.deleteBook(book);
        return new ResponseEntity<>("book successfully deleted!!",HttpStatus.OK);
    }

    @DeleteMapping("/deleteBook/{title}")
    public ResponseEntity<String> deleteBook(@PathVariable("title") String title){
        bookService.deleteBookByTitle(title);
        return new ResponseEntity<>("book successfully deleted!!",HttpStatus.OK);
    }
}
