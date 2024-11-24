package app.adapters.out.MySQL.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "authors")
public class AuthorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID authorId;
    private String name;
    private String bio;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<BookEntity> books = new HashSet<>();

    public AuthorEntity() {

    }
}
