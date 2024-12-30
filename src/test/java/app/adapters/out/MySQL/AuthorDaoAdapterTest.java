package app.adapters.out.MySQL;

import app.adapters.out.MySQL.entity.AuthorEntity;
import app.adapters.out.MySQL.entity.BookEntity;
import app.adapters.out.MySQL.repositories.AuthorRepository;
import app.domain.models.Author;
import app.infrastructure.exceptions.AuthorNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorDaoAdapterTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorDaoAdapter authorDaoAdapter;

    private Author testAuthor;

    @BeforeEach
    void setUp() {
        testAuthor = new Author(UUID.randomUUID(), "Author Name", "Bio", new HashSet<>());
    }

    @Test
    void test_addAuthor() {
        authorDaoAdapter.addAuthor(testAuthor);
        ArgumentCaptor<AuthorEntity> captor = ArgumentCaptor.forClass(AuthorEntity.class);
        verify(authorRepository).save(captor.capture());
        AuthorEntity savedAuthorEntity = captor.getValue();
        assertEquals(testAuthor.getName(), savedAuthorEntity.getName());
        assertEquals(testAuthor.getBio(), savedAuthorEntity.getBio());
    }

    @Test
    void test_getPaginatedAuthors() {
        // Arrange
        PageRequest pageable = PageRequest.of(0, 10);
        AuthorEntity authorEntity = new AuthorEntity(UUID.randomUUID(), "Author Name", "Bio", new HashSet<>());
        Page<AuthorEntity> authorEntities = new PageImpl<>(List.of(authorEntity), pageable, 1);

        when(authorRepository.findAllAuthorsWithBooks(pageable)).thenReturn(authorEntities);

        // Act
        Page<Author> result = authorDaoAdapter.getPaginatedAuthors(pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Author Name", result.getContent().getFirst().getName());
    }

    @Test
    void test_searchAuthors() {
        // Arrange
        String query = "Author";
        PageRequest pageable = PageRequest.of(0, 10);
        AuthorEntity authorEntity = new AuthorEntity(UUID.randomUUID(), "Author Name", "Bio", new HashSet<>());
        Page<AuthorEntity> authorEntities = new PageImpl<>(List.of(authorEntity), pageable, 1);

        when(authorRepository.searchAuthorsByQuery(any(), any())).thenReturn(authorEntities);

        // Act
        Page<Author> result = authorDaoAdapter.searchAuthors(query, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("Author Name", result.getContent().getFirst().getName());
    }

    @Test
    void test_updateAuthor() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        Author newAuthor = new Author(authorId, "Updated Name", "Updated Bio", new HashSet<>());
        AuthorEntity existingAuthorEntity = new AuthorEntity(authorId, "Old Name", "Old Bio", new HashSet<>());

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(existingAuthorEntity));

        // Act
        authorDaoAdapter.updateAuthor(authorId, newAuthor);

        // Assert
        assertEquals("Updated Name", existingAuthorEntity.getName());
        assertEquals("Updated Bio", existingAuthorEntity.getBio());
        verify(authorRepository).save(existingAuthorEntity);
    }

    @Test
    void test_updateAuthor_throwsException() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        Author newAuthor = new Author(authorId, "Updated Name", "Updated Bio", new HashSet<>());

        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AuthorNotFoundException.class, () -> authorDaoAdapter.updateAuthor(authorId, newAuthor));
    }

    @Test
    void test_deleteAuthor() {
        // Arrange
        UUID authorId = UUID.randomUUID();

        // Act
        authorDaoAdapter.deleteAuthor(authorId);

        // Assert
        verify(authorRepository).deleteById(authorId);
    }

    @Test
    void test_searchAuthorByName() {
        // Arrange
        String authorName = "Author Name";
        Set<BookEntity> bookEntities = new HashSet<>();
        bookEntities.add(new BookEntity(UUID.randomUUID(), "Book Title 1", "1234567890", 2022, true, LocalDate.now(), new HashSet<>(), new ArrayList<>()));
        bookEntities.add(new BookEntity(UUID.randomUUID(), "Book Title 2", "0987654321", 2023, false, LocalDate.now(), new HashSet<>(), new ArrayList<>()));

// Create AuthorEntity and associate the BookEntities
        AuthorEntity authorEntity = new AuthorEntity(
                UUID.randomUUID(),
                authorName,
                "Bio",
                bookEntities // Add the BookEntity objects to the author's books collection
        );

        when(authorRepository.findByName(authorName)).thenReturn(Optional.of(authorEntity));

        // Act
        Optional<Author> result = authorDaoAdapter.searchAuthorByName(authorName);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(authorName, result.get().getName());
    }

    @Test
    void test_searchAuthorByName_notFound() {
        // Arrange
        String authorName = "Nonexistent Author";
        when(authorRepository.findByName(authorName)).thenReturn(Optional.empty());

        // Act
        Optional<Author> result = authorDaoAdapter.searchAuthorByName(authorName);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void test_searchAuthorByID() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        AuthorEntity authorEntity = new AuthorEntity(authorId, "Author Name", "Bio", new HashSet<>());
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(authorEntity));

        // Act
        Optional<Author> result = authorDaoAdapter.searchAuthorByID(authorId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(authorId, result.get().getAuthorId());
    }

    @Test
    void test_searchAuthorByID_notFound() {
        // Arrange
        UUID authorId = UUID.randomUUID();
        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        // Act
        Optional<Author> result = authorDaoAdapter.searchAuthorByID(authorId);

        // Assert
        assertFalse(result.isPresent());
    }
}
