package app.persistence.entity;

import jakarta.persistence.*;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookId;
    private String title;
    private String isbn;
    private int publicationYear;
    private boolean availability;
    private LocalDate created_at;
    @ManyToMany
    private Set<Author> authors;
    @OneToMany(mappedBy = "book")
    private List<Transaction> transactions = new ArrayList<>();

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }
}
