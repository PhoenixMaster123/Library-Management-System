package app.persistence.repository;

import app.persistence.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Integer> {

    @Query("""
            SELECT b FROM Book b
            JOIN Author a
            WHERE b.title = :title OR b.isbn = :isbn OR a.name = :authorName
            """)
    List<Book> findAllByMultipleCriteria(String title, String authorName, String isbn);

    // TODO: Make it compliant to JPA method naming convention
    boolean isAvailable(Integer bookId);
    boolean existsByBookIdAndAvailability(Integer bookId, boolean available);
}
