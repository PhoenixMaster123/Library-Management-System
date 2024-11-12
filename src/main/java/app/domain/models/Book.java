package app.domain.models;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
//
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

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    public void addAuthor(Author author) {
        authors.add(author);
    }

    public void removeAuthor(Author author) {
        authors.remove(author);
    }
}
