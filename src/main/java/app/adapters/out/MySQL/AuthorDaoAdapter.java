package app.adapters.out.MySQL;

import app.adapters.out.MySQL.entity.AuthorEntity;
import app.adapters.out.MySQL.repositories.AuthorRepository;
import app.domain.models.Author;
import app.domain.models.Book;
import app.domain.port.AuthorDao;
import app.infrastructure.exceptions.AuthorNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AuthorDaoAdapter implements AuthorDao {
    private final AuthorRepository authorRepository;

    public AuthorDaoAdapter(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }
    @Override
    public void addAuthor(Author author) {
        AuthorEntity authorEntity = AuthorEntity.builder()
                .authorId(author.getAuthorId()) // Use the provided ID if not null
                .name(author.getName())
                .bio(author.getBio())
                .build();
        authorRepository.save(authorEntity);
    }
    @Override
    public Page<Author> getPaginatedAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable).map(authorEntity -> new Author(
                authorEntity.getAuthorId(),
                authorEntity.getName(),
                authorEntity.getBio()
        ));
    }

    @Override
    public Page<Author> searchAuthors(String query, Pageable pageable) {
        return authorRepository.findByNameContainingIgnoreCase(query, pageable).map(this::mapToAuthor);
    }

    @Override
    public void updateAuthor(UUID authorId, Author newAuthor) {
        AuthorEntity authorEntity = authorRepository.findById(authorId)
                .orElseThrow(() -> new AuthorNotFoundException("Author with ID " + authorId + " not found"));

        authorEntity.setName(newAuthor.getName());
        authorEntity.setBio(newAuthor.getBio());
        authorRepository.save(authorEntity);
    }

    @Override
    public void deleteAuthor(UUID id) {
        authorRepository.deleteById(id);
    }

    @Override
    public Optional<Author> searchAuthorByName(String name) {
        return authorRepository.findByName(name).map(this::mapToAuthor);
    }
    private Author mapToAuthor(AuthorEntity authorEntity) {
        return new Author(
                authorEntity.getAuthorId(),
                authorEntity.getName(),
                authorEntity.getBio(),
                authorEntity.getBooks() != null
                        ? authorEntity.getBooks().stream()
                        .map(bookEntity -> new Book(
                                bookEntity.getBookId(),
                                bookEntity.getTitle(),
                                bookEntity.getIsbn(),
                                bookEntity.getPublicationYear(),
                                bookEntity.isAvailability(),
                                bookEntity.getCreated_at(),
                                new HashSet<>() // Avoid circular references
                        ))
                        .collect(Collectors.toSet())
                        : new HashSet<>()
        );
    }
}
