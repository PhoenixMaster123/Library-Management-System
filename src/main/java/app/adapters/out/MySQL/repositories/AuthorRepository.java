package app.adapters.out.MySQL.repositories;

import app.adapters.out.MySQL.entity.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AuthorRepository extends JpaRepository<AuthorEntity, UUID> {
    @Modifying
    @Query("UPDATE AuthorEntity a SET a.name = :#{#newAuthor.name}, a.bio = :#{#newAuthor.bio} " +
            "WHERE a.authorId = :#{#newAuthor.authorId}")
    int updateAuthorDetails(@Param("newAuthor") AuthorEntity newAuthor);

}
