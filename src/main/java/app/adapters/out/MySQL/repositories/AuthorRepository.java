package app.adapters.out.MySQL.repositories;

import app.adapters.out.MySQL.entity.AuthorEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthorRepository extends JpaRepository<AuthorEntity, UUID> {
    Optional<AuthorEntity> findByName(String name);

    @Query("SELECT a FROM AuthorEntity a LEFT JOIN FETCH a.books")
    List<AuthorEntity> findAllAuthorsWithBooks(Pageable pageable);

    @Query("SELECT a FROM AuthorEntity a LEFT JOIN a.books b " +
            "WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR CAST(a.authorId AS string) LIKE CONCAT('%', :query, '%') " +
            "OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))")
    Optional<AuthorEntity> searchAuthorsByQuery(@Param("query") String query);

}
