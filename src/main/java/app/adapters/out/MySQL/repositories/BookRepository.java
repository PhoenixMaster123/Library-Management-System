package app.adapters.out.MySQL.repositories;

import app.adapters.out.MySQL.entity.BookEntity;
import app.domain.models.Book;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface BookRepository extends JpaRepository<BookEntity, UUID> {
    @Query("SELECT b FROM BookEntity b WHERE b.title = :title")
    Optional<BookEntity> findBookByTitle(@Param("title") String title);

    /*@Query("SELECT b FROM BookEntity b JOIN b.authors a WHERE a.name = :authorName")
    List<BookEntity> findBooksByAuthorName(@Param("authorName") String authorName);
     */

    @Query("SELECT b FROM BookEntity b JOIN b.authors a WHERE a.name = :author AND b.availability = :isAvailable")
    List<BookEntity> findBooksByAuthor(@Param("author") String author, @Param("isAvailable") boolean isAvailable);

    @Query("SELECT b FROM BookEntity b WHERE b.isbn = :isbn")
    Optional<BookEntity> findBooksByIsbn(@Param("isbn") String isbn);

    @Modifying
    @Query("UPDATE BookEntity b SET b.availability = :#{#book.availability} WHERE b.bookId = :#{#book.bookId}")
    int updateBook(@Param("book") BookEntity book);

    void deleteByTitle(String title);
}
