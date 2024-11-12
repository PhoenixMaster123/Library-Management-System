package app.persistence.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer authorId;
    private String name;
    private String bio;

    @ManyToMany
    private Set<Book> books = new HashSet<>();
}
