package app.domain.services;

import app.adapters.in.dto.CreateNewAuthor;
import app.domain.models.Author;
import app.domain.port.AuthorDao;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthorService {
    private final AuthorDao authorDao;
    public AuthorService(AuthorDao authorDao) {
        this.authorDao = authorDao;
    }
    public Author createNewAuthor(CreateNewAuthor createNewAuthor) {
        if (authorDao.searchAuthorByName(createNewAuthor.getName()).isPresent()) {
            throw new IllegalArgumentException("Author with the same name already exists.");
        }
        Author author = new Author(createNewAuthor.getName(), createNewAuthor.getBio());
        authorDao.addAuthor(author);
        return authorDao.searchAuthorByName(createNewAuthor.getName()) // Retrieve the persisted entity with the generated ID
                .orElseThrow(() -> new IllegalStateException("Author was not properly saved"));
    }
    public void updateAuthor(UUID authorId, Author author) {
        authorDao.updateAuthor(authorId, author);
    }
    public void deleteAuthor(UUID id) {
        authorDao.deleteAuthor(id);
    }
    public Optional<Author> getAuthorByName(String name) {
        return authorDao.searchAuthorByName(name);
    }
    public Page<Author> getPaginatedAuthors(Pageable pageable) {
        return authorDao.getPaginatedAuthors(pageable);
    }
    public Page<Author> searchAuthors(String query, Pageable pageable) {
        return authorDao.searchAuthors(query, pageable);
    }
    public Optional<Author> findAuthorById(UUID authorId) {
        return authorDao.searchAuthorByID(authorId);
    }
}
