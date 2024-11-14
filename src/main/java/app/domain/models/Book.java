package app.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class Book {
    private Integer bookId;
    private String title;
    private String isbn;
    private int publicationYear;
    private boolean isAvailable;
    private LocalDate createdAt;
    private final Set<Author> authors = new HashSet<>();

    public Book(Integer bookId, String title, String isbn, int publicationYear, boolean isAvailable, LocalDate createdAt) {
        this.bookId = bookId;
        this.title = title;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.isAvailable = isAvailable;
        this.createdAt = createdAt;
    }
    public void addAuthor(Author author) {
        authors.add(author);
    }

    public void removeAuthor(Author author) {
        authors.remove(author);
    }
}
