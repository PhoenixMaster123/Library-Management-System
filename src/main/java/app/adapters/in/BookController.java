package app.adapters.in;

import app.adapters.in.dto.CreateNewBook;
import app.domain.models.Book;
import app.domain.services.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<Book> createNewBook(@Valid @RequestBody CreateNewBook newBook) {

        Book book = bookService.createNewBook(newBook);

        return ResponseEntity.ok(book);
    }
    @GetMapping(value = "/title/{title}", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<?> getBookByTitle(@NotNull @PathVariable("title") String title) {
        return ResponseEntity.ok(bookService.searchBookByTitle(title));
    }
    @GetMapping(value = "/author/{author}", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<?> getBookByAuthor(@NotNull @PathVariable("author") String author) {
        return ResponseEntity.ok(bookService.searchBookByAuthors(author, true));
    }
    @GetMapping(value = "/{isbn}",produces = "application/single-book-response+json;version=1")
    public ResponseEntity<?> getBookByIsbn(@NotNull @PathVariable("isbn") String isbn) {
        return ResponseEntity.ok(bookService.searchByIsbn(isbn));
    }
    @GetMapping(value = "/paginated", produces = "application/paginated-books-response+json;version=1")
    public ResponseEntity<Page<Book>> getPaginatedBooks(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam Optional<String> sortBy
    ) {
        PageRequest pageable = PageRequest.of(
                page.orElse(0),  // Default to page 0
                size.orElse(10), // Default to 10 items per page
                Sort.Direction.ASC,
                sortBy.orElse("title") // Default sort field
        );

        return ResponseEntity.ok(bookService.getPaginatedBooks(pageable));
    }
    // General search
    @GetMapping(value = "/search/paginated", produces = "application/json;version=1")
    public ResponseEntity<Page<Book>> searchBooks(
            @RequestParam("query") String query,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam Optional<String> sortBy
    ) {
        PageRequest pageable = PageRequest.of(
                page.orElse(0),  // Default to page 0
                size.orElse(10), // Default to 10 items per page
                Sort.Direction.ASC,
                sortBy.orElse("title") // Default sort field
        );

        Page<Book> books = bookService.searchBooks(query, pageable);
        return ResponseEntity.ok(books);
    }



    @PutMapping(value = "/{id}", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<String> updateBook(@NotNull @PathVariable("id") UUID id, @NotNull @RequestBody Book book) {
        Optional<Book> existingBook = bookService.searchById(id);
        if (existingBook.isEmpty()) {
            return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
        }
        book.setBookId(id);
        bookService.updateBook(id, book);
        return new ResponseEntity<>("Book updated successfully", HttpStatus.OK);
    }
    @DeleteMapping("/deleteBookById/{id}")
    public ResponseEntity<String> deleteBook(@NotNull @PathVariable("id") UUID bookID) {
        bookService.deleteBook(bookID);
        return new ResponseEntity<>("Book successfully deleted!!", HttpStatus.OK);
    }
}
