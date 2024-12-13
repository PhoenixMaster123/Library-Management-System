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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

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
    @GetMapping(value = "/paginated", produces = "application/paginated-books-response+json;version=1")
    public ResponseEntity<EntityModel<Page<Book>>> getAllBooks(
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
                .getAllBooks(Optional.of(currentPage), Optional.of(pageSize), Optional.of(sortField)))
                .withSelfRel());

        // Add previous link if there is a previous page
        if (books.hasPrevious()) {
            resource.add(linkTo(methodOn(BookController.class)
                    .getAllBooks(Optional.of(currentPage - 1), Optional.of(pageSize), Optional.of(sortField)))
                    .withRel("prev"));
        }

        // Add next link if there is a next page
        if (books.hasNext()) {
            resource.add(linkTo(methodOn(BookController.class)
                    .getAllBooks(Optional.of(currentPage + 1), Optional.of(pageSize), Optional.of(sortField)))
                    .withRel("next"));
        }

        // Add links to response headers instead of body
        HttpHeaders headers = new HttpHeaders();
        headers.add("Link", "<" + linkTo(methodOn(BookController.class)
                .getAllBooks(Optional.of(currentPage), Optional.of(pageSize), Optional.of(sortField))).toUri() + ">; rel=\"self\"");
        if (books.hasPrevious()) {
            headers.add("Link", "<" + linkTo(methodOn(BookController.class)
                    .getAllBooks(Optional.of(currentPage - 1), Optional.of(pageSize), Optional.of(sortField))).toUri() + ">; rel=\"prev\"");
        }
        if (books.hasNext()) {
            headers.add("Link", "<" + linkTo(methodOn(BookController.class)
                    .getAllBooks(Optional.of(currentPage + 1), Optional.of(pageSize), Optional.of(sortField))).toUri() + ">; rel=\"next\"");
        }

        return ResponseEntity.ok().headers(headers).body(resource);
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
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBook(@NotNull @PathVariable("id") UUID bookID) {
        bookService.deleteBook(bookID);
        return new ResponseEntity<>("Book successfully deleted!!", HttpStatus.OK);
    }
    @GetMapping(produces = "application/single-book-response+json;version=1")
    public ResponseEntity<?> getBook(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String query,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam Optional<String> sortBy
    ) {
        if (id != null) {
            Optional<Book> book = bookService.searchById(id);
            if (book.isEmpty()) {
                return new ResponseEntity<>("Book not found", HttpStatus.NOT_FOUND);
            }

            EntityModel<Book> resource = EntityModel.of(book.get());
            resource.add(linkTo(methodOn(BookController.class).getBook(id, null, null, null, null, Optional.empty(), Optional.empty(), Optional.empty())).withSelfRel());
            resource.add(linkTo(methodOn(BookController.class).updateBook(id, book.get())).withRel("update"));
            resource.add(linkTo(methodOn(BookController.class).deleteBook(id)).withRel("delete"));

            return ResponseEntity.ok(resource);
        } else if (title != null) {
            Optional<Book> book = bookService.searchBookByTitle(title);
            if (book.isEmpty()) {
                return new ResponseEntity<>("Book with the given title not found", HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok(book);
        } else if (isbn != null) {
            Optional<Book> book = bookService.searchByIsbn(isbn);
            if (book.isEmpty()) {
                return new ResponseEntity<>("Book with the given ISBN not found", HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok(book);
        } else if (author != null) {
            Optional<Book> book = bookService.searchBookByAuthors(author, true);
            if (book.isEmpty()) {
                return new ResponseEntity<>("No books found by the given author", HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok(book);
        } else if (query != null) {
            int currentPage = page.orElse(0);
            int pageSize = size.orElse(10);
            String sortField = sortBy.orElse("title");

            PageRequest pageable = PageRequest.of(currentPage, pageSize, Sort.Direction.ASC, sortField);
            Page<Book> books = bookService.searchBooks(query, pageable);

            if (books.isEmpty()) {
                return new ResponseEntity<>("No books found for the given query", HttpStatus.NOT_FOUND);
            }

            EntityModel<Page<Book>> resource = EntityModel.of(books);
            resource.add(linkTo(methodOn(BookController.class)
                    .getBook(null, null, null, null, query, Optional.of(currentPage), Optional.of(pageSize), Optional.of(sortField)))
                    .withSelfRel());

            if (books.hasPrevious()) {
                resource.add(linkTo(methodOn(BookController.class)
                        .getBook(null, null, null, null, query, Optional.of(currentPage - 1), Optional.of(pageSize), Optional.of(sortField)))
                        .withRel("prev"));
            }

            if (books.hasNext()) {
                resource.add(linkTo(methodOn(BookController.class)
                        .getBook(null, null, null, null, query, Optional.of(currentPage + 1), Optional.of(pageSize), Optional.of(sortField)))
                        .withRel("next"));
            }

            return ResponseEntity.ok(resource);
        } else {
            return new ResponseEntity<>("No search criteria provided", HttpStatus.BAD_REQUEST);
        }
    }
}
