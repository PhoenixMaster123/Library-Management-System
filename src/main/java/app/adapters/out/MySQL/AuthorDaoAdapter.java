package app.adapters.out.MySQL;

import app.adapters.out.MySQL.entity.AuthorEntity;
import app.adapters.out.MySQL.repositories.AuthorRepository;
import app.domain.models.Author;
import app.domain.port.AuthorDao;
import org.springframework.stereotype.Component;

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
    public void updateAuthorDetails(Author author) {
        AuthorEntity entity = authorRepository.findById(author.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        entity.setName(author.getName());
        entity.setBio(author.getBio());

        authorRepository.save(entity);
    }

    @Override
    public void deleteAuthor(UUID id) {
        authorRepository.deleteById(id);
    }
}
