package app.domain.port;

import app.domain.models.Author;
import app.domain.models.Book;

import java.util.Optional;
import java.util.UUID;

public interface AuthorDao {
    void addAuthor(Author author);
    void updateAuthor(UUID authorId, Author author);
    void deleteAuthor(UUID id);
    Optional<Author> searchAuthorByName(String name);
}
