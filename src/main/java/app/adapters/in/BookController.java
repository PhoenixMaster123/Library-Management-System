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
import org.springframework.hateoas.EntityModel;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
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
    @GetMapping(value = "{id}", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<?> getBookById(@NotNull @PathVariable("id") UUID id) {
        Optional<Book> book = bookService.searchById(id);

        if (book.isEmpty()) {
            return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(book.get());
    }
    @GetMapping(value = "/title/{title}", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<?> getBookByTitle(@NotNull @PathVariable("title") String title) {
        //return ResponseEntity.ok(bookService.searchBookByTitle(title));
        Optional<Book> book = bookService.searchBookByTitle(title);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(60, TimeUnit.SECONDS))  // Cache for 60 seconds
                .body(book);
    }
    @GetMapping(value = "/author/{author}", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<?> getBookByAuthor(@NotNull @PathVariable("author") String author) {
        return ResponseEntity.ok(bookService.searchBookByAuthors(author, true));
    }
    @GetMapping(value = "/isbn/{isbn}",produces = "application/single-book-response+json;version=1")
    public ResponseEntity<?> getBookByIsbn(@NotNull @PathVariable("isbn") String isbn) {
        return ResponseEntity.ok(bookService.searchByIsbn(isbn));
    }
    @GetMapping(value = "/paginated", produces = "application/paginated-books-response+json;version=1")
    public ResponseEntity<EntityModel<Page<Book>>> getPaginatedBooks(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam Optional<String> sortBy
    ) {
        int currentPage = page.orElse(0);
        int pageSize = size.orElse(1);
        String sortField = sortBy.orElse("title");

        PageRequest pageable = PageRequest.of(currentPage, pageSize, Sort.Direction.ASC, sortField);
        Page<Book> books = bookService.getPaginatedBooks(pageable);

        // Create self link with actual values
        EntityModel<Page<Book>> resource = EntityModel.of(books);
        resource.add(linkTo(methodOn(BookController.class)
                .getPaginatedBooks(Optional.of(currentPage), Optional.of(pageSize), Optional.of(sortField)))
                .withSelfRel());

        // Add previous link if there is a previous page
        if (books.hasPrevious()) {
            resource.add(linkTo(methodOn(BookController.class)
                    .getPaginatedBooks(Optional.of(currentPage - 1), Optional.of(pageSize), Optional.of(sortField)))
                    .withRel("prev"));
        }

        // Add next link if there is a next page
        if (books.hasNext()) {
            resource.add(linkTo(methodOn(BookController.class)
                    .getPaginatedBooks(Optional.of(currentPage + 1), Optional.of(pageSize), Optional.of(sortField)))
                    .withRel("next"));
        }

        // Add links to response headers instead of body
        HttpHeaders headers = new HttpHeaders();
        headers.add("Link", "<" + linkTo(methodOn(BookController.class)
                .getPaginatedBooks(Optional.of(currentPage), Optional.of(pageSize), Optional.of(sortField))).toUri() + ">; rel=\"self\"");
        if (books.hasPrevious()) {
            headers.add("Link", "<" + linkTo(methodOn(BookController.class)
                    .getPaginatedBooks(Optional.of(currentPage - 1), Optional.of(pageSize), Optional.of(sortField))).toUri() + ">; rel=\"prev\"");
        }
        if (books.hasNext()) {
            headers.add("Link", "<" + linkTo(methodOn(BookController.class)
                    .getPaginatedBooks(Optional.of(currentPage + 1), Optional.of(pageSize), Optional.of(sortField))).toUri() + ">; rel=\"next\"");
        }

        return ResponseEntity.ok().headers(headers).body(resource);
    }
    // General search
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
