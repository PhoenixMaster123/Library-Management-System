package app.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
public class Author {
    private UUID authorId;
    private String name;
    private String bio;
    private final Set<Book> books = new HashSet<>();

    public Author(String name, String bio) {
        this.authorId = UUID.randomUUID();
        this.name = name;
        this.bio = bio;
    }

    public Author() {

    }
}
