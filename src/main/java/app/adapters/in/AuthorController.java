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
        int pageSize = size.orElse(1);
        String sortField = sortBy.orElse("name");

        PageRequest pageable = PageRequest.of(currentPage, pageSize, Sort.Direction.ASC, sortField);
        Page<Author> authors = authorService.getPaginatedAuthors(pageable);

        if (authors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No authors found"));
        }

        // Construct the response map
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", authors.getContent());
        response.put("totalPages", authors.getTotalPages());
        response.put("currentPage", authors.getNumber());
        response.put("totalItems", authors.getTotalElements());

        // Add links directly to the root of the response
        HttpHeaders headers = new HttpHeaders();
        headers.add("self", generatePaginatedLink(currentPage, pageSize, sortField));
        if (authors.hasPrevious()) {
            headers.add("prev", generatePaginatedLink(currentPage - 1, pageSize, sortField));
        }
        if (authors.hasNext()) {
            headers.add("next", generatePaginatedLink(currentPage + 1, pageSize, sortField));
        }

        return ResponseEntity.ok().headers(headers).body(response);
    }

    private String generatePaginatedLink(int page, int size, String sortBy) {
        return linkTo(AuthorController.class).slash("paginated").toUriComponentsBuilder()
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortBy", sortBy)
                .toUriString();
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
