package app.adapters.in;

import app.adapters.in.dto.CreateNewAuthor;
import app.domain.models.Author;
import app.domain.services.AuthorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/authors")
public class AuthorController {
    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @PostMapping(produces = "application/single-book-response+json;version=1")
    public ResponseEntity<Author> createNewAuthor(@Valid @RequestBody CreateNewAuthor newAuthor) {

        Author author = authorService.createNewAuthor(newAuthor);

        return ResponseEntity.ok(author);
    }
    @GetMapping(value = "/search", produces = "application/single-author-response+json;version=1")
    public ResponseEntity<Author> getAuthor(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String query) {
        Optional<Author> author;

        if (id != null) {
            author = authorService.findAuthorById(id);
        } else if (name != null && !name.isBlank()) {
            author = authorService.getAuthorByName(name);
        } else if (query != null && !query.isBlank()) {
            author = authorService.searchAuthors(query);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        return author.map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping(value = "/paginated", produces = "application/paginated-authors-response+json;version=1")
    public ResponseEntity<Map<String, Object>> getAllAuthors(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam Optional<String> sortBy
    ) {
        int currentPage = page.orElse(0);
        int pageSize = size.orElse(3);
        String sortField = sortBy.orElse("name");

        PageRequest pageable = PageRequest.of(currentPage, pageSize, Sort.Direction.ASC, sortField);
        Page<Author> authors = authorService.getPaginatedAuthors(pageable);

        // Build headers with pagination links
        HttpHeaders headers = new HttpHeaders();
        headers.add("self", "<" + linkTo(methodOn(AuthorController.class)
                .getAllAuthors(Optional.of(currentPage), Optional.of(pageSize), Optional.of(sortField))).toUri() + ">; rel=\"self\"");

        // Add next link if there's a next page
        if (authors.hasNext()) {
            headers.add("next", "<" + linkTo(methodOn(AuthorController.class)
                    .getAllAuthors(Optional.of(currentPage + 1), Optional.of(pageSize), Optional.of(sortField))).toUri() + ">; rel=\"next\"");
        }

        // Add previous link if there's a previous page
        if (authors.hasPrevious()) {
            headers.add("prev", "<" + linkTo(methodOn(AuthorController.class)
                    .getAllAuthors(Optional.of(currentPage - 1), Optional.of(pageSize), Optional.of(sortField))).toUri() + ">; rel=\"prev\"");
        }

        // Build response body
        if (authors.isEmpty()) {
            Map<String, Object> errorResponse = Map.of(
                    "message", "There are no authors on this page.",
                    "currentPage", currentPage,
                    "pageSize", pageSize,
                    "sortBy", sortField
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).headers(headers).body(errorResponse);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", authors.getContent());
        response.put("totalPages", authors.getTotalPages());
        response.put("currentPage", authors.getNumber());
        response.put("totalItems", authors.getTotalElements());

        return ResponseEntity.ok().headers(headers).body(response);
    }
    @PutMapping(value = "/{authorId}", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<String> updateAuthor(@NotNull @PathVariable UUID authorId, @Valid @RequestBody Author author) {

        author.setAuthorId(authorId);
        authorService.updateAuthor(authorId, author);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Author updated successfully!");
    }
    /*@DeleteMapping("/deleteAuthorById/{id}")
    public ResponseEntity<String> deleteAuthor(@NotNull @PathVariable UUID id) {
        authorService.deleteAuthor(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Author deleted successfully!");
    }
     */
}
