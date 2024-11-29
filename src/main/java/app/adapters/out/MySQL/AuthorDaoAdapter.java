package app.adapters.out.MySQL;

import app.adapters.out.MySQL.entity.AuthorEntity;
import app.adapters.out.MySQL.repositories.AuthorRepository;
import app.domain.models.Author;
import app.domain.port.AuthorDao;
import app.infrastructure.exceptions.AuthorNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AuthorDaoAdapter implements AuthorDao {
    private final AuthorRepository authorRepository;

    public AuthorDaoAdapter(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }
    @Override
    public void addAuthor(Author author) {
        AuthorEntity authorEntity = AuthorEntity.builder()
                .authorId(author.getAuthorId())
                .name(author.getName())
                .bio(author.getBio())
                .build();
        authorRepository.save(authorEntity);
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
        return authorRepository.findByName(name)
                .map(authorEntity -> new Author(
                        authorEntity.getAuthorId(),
                        authorEntity.getName(),
                        authorEntity.getBio()
                ));
    }
}
