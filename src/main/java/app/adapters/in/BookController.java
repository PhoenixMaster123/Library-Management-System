package app.adapters.in;

import app.adapters.in.dto.CreateNewBook;
import app.domain.models.Book;
import app.domain.services.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    @Cacheable(value = "books", key = "#title")
    @GetMapping(value = "/title/{title}", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<?> getBookByTitle(@NotNull @PathVariable("title") String title) {
        //return ResponseEntity.ok(bookService.searchBookByTitle(title));
        Optional<Book> book = bookService.searchBookByTitle(title);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))  // Cache for 60 seconds
                .body(book);
    }
    @Cacheable(value = "books", key = "#author")
    @GetMapping(value = "/author/{author}", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<?> getBookByAuthor(@NotNull @PathVariable("author") String author) {
        return ResponseEntity.ok(bookService.searchBookByAuthors(author, true));
    }
    @Cacheable(value = "books", key = "#isbn")
    @GetMapping(value = "/isbn/{isbn}",produces = "application/single-book-response+json;version=1")
    public ResponseEntity<?> getBookByIsbn(@NotNull @PathVariable("isbn") String isbn) {
        return ResponseEntity.ok(bookService.searchByIsbn(isbn));
    }
    @Cacheable(value = "books", key = "#page.orElse(0) + '-' + #size.orElse(10) + '-' + #sortBy.orElse('title')")
    @GetMapping(value = "/paginated", produces = "application/paginated-books-response+json;version=1")
    public ResponseEntity<EntityModel<Page<Book>>> getPaginatedBooks(
            @RequestParam @Min(0) Optional<Integer> page,
            @RequestParam  @Min(1) @Max(100) Optional<Integer> size,
            @RequestParam Optional<String> sortBy
    ) {
        PageRequest pageable = PageRequest.of(
                page.orElse(0),
                size.orElse(10),
                Sort.Direction.ASC,
                sortBy.orElse("title")
        );


        Page<Book> books = bookService.getPaginatedBooks(pageable);
        EntityModel<Page<Book>> resource = EntityModel.of(books);
        resource.add(linkTo(methodOn(BookController.class).getPaginatedBooks(page, size, sortBy)).withSelfRel());

        if (books.hasPrevious()) {
            resource.add(linkTo(methodOn(BookController.class)
                    .getPaginatedBooks(Optional.of(page.orElse(0) - 1), size, sortBy))
                    .withRel("prev"));
        }

        if (books.hasNext()) {
            resource.add(linkTo(methodOn(BookController.class)
                    .getPaginatedBooks(Optional.of(page.orElse(0) + 1), size, sortBy))
                    .withRel("next"));
        }

        return ResponseEntity.ok(resource);
    }
    @Cacheable(value = "books", key = "#query + '-' + #queryParam.orElse('') + '-' + #page.orElse(0) + '-' + #size.orElse(10) + '-' + #sortBy.orElse('title')")
    @GetMapping(value = {"/search/paginated", "/search/paginated/{query}"},
            produces = {"application/json;version=1", "application/paginated-books-response+json;version=1"})
    public ResponseEntity<Page<Book>> searchBooks(
            @PathVariable(required = false) String query,
            @RequestParam Optional<String> queryParam,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam Optional<String> sortBy
    ) {
        // Determine the query from either the path variable or the query parameter
        String searchQuery = query != null ? query : queryParam.orElse("");

        PageRequest pageable = PageRequest.of(
                page.orElse(0),  // Default to page 0
                size.orElse(10), // Default to 10 items per page
                Sort.Direction.ASC,
                sortBy.orElse("title") // Default sort field
        );

        Page<Book> books = bookService.searchBooks(searchQuery, pageable);
        return ResponseEntity.ok(books);
    }
    // General search
    /*@GetMapping(value = "/search/paginated", produces = "application/json;version=1")
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
    @GetMapping(value = "/search/paginated/{query}", produces = "application/paginated-books-response+json;version=1")
    public ResponseEntity<Page<Book>> searchBooksByPath(
            @PathVariable String query,
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
     */

    @PutMapping(value = "/updateBook/{id}", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<String> updateBook(@NotNull @PathVariable("id") UUID id, @NotNull @RequestBody Book book) {
        Optional<Book> existingBook = bookService.searchById(id);
        if (existingBook.isEmpty()) {
            return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
        }
        book.setBookId(id);
        bookService.updateBook(id, book);
        return new ResponseEntity<>("Book updated successfully", HttpStatus.OK);
    }
    @DeleteMapping("/deleteBook/{id}")
    public ResponseEntity<String> deleteBook(@NotNull @PathVariable("id") UUID bookID) {
        bookService.deleteBook(bookID);
        return new ResponseEntity<>("Book successfully deleted!!", HttpStatus.OK);
    }
}
