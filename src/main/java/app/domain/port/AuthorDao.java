package app.domain.port;

import app.domain.models.Author;

import java.util.UUID;

public interface AuthorDao {
    void addAuthor(Author author);
    void updateAuthorDetails(Author author);
    void deleteAuthor(UUID id);

}
