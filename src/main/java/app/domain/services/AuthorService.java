package app.domain.services;

import app.adapters.in.dto.CreateNewAuthor;
import app.domain.models.Author;
import app.domain.models.Book;
import app.domain.port.AuthorDao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
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
        return author;
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
}
