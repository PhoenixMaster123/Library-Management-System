package app.domain.services;

import app.adapters.in.dto.CreateNewAuthor;
import app.domain.models.Author;
import app.domain.models.Book;
import app.domain.port.AuthorDao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class AuthorService {
    private final AuthorDao authorDao;

    public AuthorService(AuthorDao authorDao) {
        this.authorDao = authorDao;
    }
    public Author createNewAuthor(CreateNewAuthor createNewAuthor) {
        Author author = new Author(createNewAuthor.getName(), createNewAuthor.getBio());
        authorDao.addAuthor(author);
        return author;
    }
    public void updateAuthor(Author author) {
        authorDao.updateAuthorDetails(author);
    }

    public void deleteAuthor(UUID id) {
        authorDao.deleteAuthor(id);
    }
}
