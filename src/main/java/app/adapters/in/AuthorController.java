package app.adapters.in;

import app.adapters.in.dto.CreateNewAuthor;
import app.adapters.in.dto.CreateNewBook;
import app.domain.models.Author;
import app.domain.models.Book;
import app.domain.services.AuthorService;
import app.domain.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/authors")
public class AuthorController {
    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping(produces = "application/single-book-response+json;version=1")
    public ResponseEntity<Author> createNewAuthor(@RequestBody CreateNewAuthor newAuthor) {

        Author author = authorService.createNewAuthor(newAuthor);

        return ResponseEntity.ok(author);
    }

    // TODO: Is it correct?
    @PutMapping("/{id}")
    public ResponseEntity<String> updateAuthor(@PathVariable UUID id, @RequestBody Author author) {
        // Set the ID explicitly to avoid mismatches
        author.setAuthorId(id);

        authorService.updateAuthor(author);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Author updated successfully!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAuthor(@PathVariable UUID id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Author deleted successfully!");
    }
}
