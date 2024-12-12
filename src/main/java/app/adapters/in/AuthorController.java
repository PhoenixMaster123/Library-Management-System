package app.adapters.in;

import app.adapters.in.dto.CreateNewAuthor;
import app.domain.models.Author;
import app.domain.services.AuthorService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.Link;
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
    @GetMapping(value = "/{name}", produces = "application/single-book-response+json;version=1")
    public ResponseEntity<Author> getAuthorByName(@NotNull @PathVariable String name) {
        return authorService.getAuthorByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    @GetMapping(value = "/paginated", produces = "application/paginated-authors-response+json;version=1")
    public ResponseEntity<Map<String, Object>> getPaginatedAuthors(
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam Optional<String> sortBy,
            @RequestParam Optional<String> query
    ) {
        int currentPage = page.orElse(0);
        int pageSize = size.orElse(1);
        String sortField = sortBy.orElse("name");

        PageRequest pageable = PageRequest.of(currentPage, pageSize, Sort.Direction.ASC, sortField);
        Page<Author> authors;

        if (query.isPresent() && !query.get().isBlank()) {
            authors = authorService.searchAuthors(query.get(), pageable);
        } else {
            authors = authorService.getPaginatedAuthors(pageable);
        }

        if (authors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No authors found"));
        }

        // Construct the response map
        Map<String, Object> response = new HashMap<>();
        response.put("data", authors.getContent());
        response.put("totalPages", authors.getTotalPages());
        response.put("currentPage", authors.getNumber());
        response.put("totalItems", authors.getTotalElements());

        // Add links directly to the root of the response
        response.put("self", generatePaginatedLink(currentPage, pageSize, sortField, query));
        if (authors.hasPrevious()) {
            response.put("prev", generatePaginatedLink(currentPage - 1, pageSize, sortField, query));
        }
        if (authors.hasNext()) {
            response.put("next", generatePaginatedLink(currentPage + 1, pageSize, sortField, query));
        }

        return ResponseEntity.ok(response);
    }

    private String generatePaginatedLink(int page, int size, String sortBy, Optional<String> query) {
        String baseUri = linkTo(AuthorController.class).slash("paginated").toUriComponentsBuilder()
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortBy", sortBy)
                .toUriString();

        if (query.isPresent() && !query.get().isBlank()) {
            return baseUri + "&query=" + query.get();
        }
        return baseUri;
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
