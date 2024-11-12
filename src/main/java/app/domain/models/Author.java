package app.domain.models;

import java.util.HashSet;
import java.util.Set;
//
public class Author {
    private Integer authorId;
    private String name;
    private String bio;
    private final Set<Book> books = new HashSet<>();

    public Author(Integer authorId, String name, String bio) {
        this.authorId = authorId;
        this.name = name;
        this.bio = bio;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Set<Book> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public void removeBook(Book book) {
        books.remove(book);
    }
}
