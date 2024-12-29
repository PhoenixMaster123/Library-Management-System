package app.domain.services;

import app.adapters.in.dto.CreateNewAuthor;
import app.adapters.out.MySQL.repositories.AuthorRepository;
import app.domain.models.Author;
import app.domain.port.AuthorDao;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthorServiceTest {

    // Mocked Dependencies for Unit Tests
    @Mock
    private AuthorDao mockedAuthorDao;

    // Real Dependencies for Integration Tests
    @Autowired
    private AuthorDao realAuthorDao;

    @Autowired
    private AuthorService realAuthorService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    void setup() {
        mockedAuthorDao = mock(AuthorDao.class);
    }

    @Nested
    @DisplayName("Unit Tests")
    class UnitTests {

        @Test
        void createNewAuthor_Success() {
            AuthorService authorService = new AuthorService(mockedAuthorDao);
            CreateNewAuthor newAuthor = new CreateNewAuthor("John Doe", "Author bio");

            when(mockedAuthorDao.searchAuthorByName("John Doe")).thenReturn(Optional.empty());
            doAnswer(invocation -> {
                Author savedAuthor = invocation.getArgument(0);
                when(mockedAuthorDao.searchAuthorByName(savedAuthor.getName())).thenReturn(Optional.of(savedAuthor));
                return null;
            }).when(mockedAuthorDao).addAuthor(any(Author.class));

            authorService.createNewAuthor(newAuthor);

            // Capture and verify
            ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
            verify(mockedAuthorDao).addAuthor(captor.capture());
            assertEquals("John Doe", captor.getValue().getName());
            assertEquals("Author bio", captor.getValue().getBio());
        }
        @Test
        void createNewAuthor_ThrowsException_WhenNameExists() {
            AuthorService authorService = new AuthorService(mockedAuthorDao);
            CreateNewAuthor newAuthor = new CreateNewAuthor("John Doe", "Author bio");

            when(mockedAuthorDao.searchAuthorByName("John Doe")).thenReturn(Optional.of(new Author()));

            assertThrows(IllegalArgumentException.class, () -> authorService.createNewAuthor(newAuthor));
        }
        @Test
        void searchAuthorByName_Found() {
            AuthorService authorService = new AuthorService(mockedAuthorDao);
            String name = "John Doe";
            Author author = new Author(name, "Author bio");

            when(mockedAuthorDao.searchAuthorByName(name)).thenReturn(Optional.of(author));

            Optional<Author> result = authorService.getAuthorByName(name);

            assertTrue(result.isPresent());
            assertEquals(name, result.get().getName());
            verify(mockedAuthorDao, times(1)).searchAuthorByName(name);
        }

        @Test
        void searchAuthorById_Found() {
            AuthorService authorService = new AuthorService(mockedAuthorDao);
            UUID authorId = UUID.randomUUID();
            Author author = new Author("John Doe", "Author bio");
            author.setAuthorId(authorId);

            when(mockedAuthorDao.searchAuthorByID(authorId)).thenReturn(Optional.of(author));

            Optional<Author> result = authorService.findAuthorById(authorId);

            assertTrue(result.isPresent());
            assertEquals("John Doe", result.get().getName());
            verify(mockedAuthorDao, times(1)).searchAuthorByID(authorId);
        }
        @Test
        void updateAuthor_Success() {
            AuthorService authorService = new AuthorService(mockedAuthorDao);
            UUID authorId = UUID.randomUUID();
            Author existingAuthor = new Author(authorId, "John Doe", "example");
            Author updatedAuthor = new Author("John Update", "updated bio");

            // Mock the behavior of the search method
            when(mockedAuthorDao.searchAuthorByID(authorId)).thenReturn(Optional.of(existingAuthor));

            // Perform the update operation
            authorService.updateAuthor(authorId, updatedAuthor);

            // Verify that the update method was called once with the correct arguments
            verify(mockedAuthorDao).updateAuthor(eq(authorId), argThat(updated -> {
                assertEquals(updatedAuthor.getName(), updated.getName());
                assertEquals(updatedAuthor.getBio(), updated.getBio());
                return true;
            }));
        }
        @Test
        public void testGetPaginatedAuthors() {
            AuthorService authorService = new AuthorService(mockedAuthorDao);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Author> mockPage = new PageImpl<>(List.of(new Author(UUID.randomUUID(),
                    "John Doe", "example"),
                    new Author(UUID.randomUUID(),"Jane Smith", "example")));
            Mockito.when(mockedAuthorDao.getPaginatedAuthors(pageable)).thenReturn(mockPage);

            // Act
            Page<Author> result = authorService.getPaginatedAuthors(pageable);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            Mockito.verify(mockedAuthorDao, Mockito.times(1)).getPaginatedAuthors(pageable);
        }

        @Test
        public void testSearchAuthors() {
            AuthorService authorService = new AuthorService(mockedAuthorDao);
            String query = "John";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Author> mockPage = new PageImpl<>(List.of(new Author("John Doe", "example")));
            Mockito.when(mockedAuthorDao.searchAuthors(query, pageable)).thenReturn(mockPage);

            // Act
            Page<Author> result = authorService.searchAuthors(query, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("John Doe", result.getContent().getFirst().getName());
            Mockito.verify(mockedAuthorDao, Mockito.times(1)).searchAuthors(query, pageable);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class IntegrationTests {

        @Test
        void createNewAuthor_IntegrationTest() {
            CreateNewAuthor newAuthor = new CreateNewAuthor("Jane Smith", "Author bio");

            Author createdAuthor = realAuthorService.createNewAuthor(newAuthor);

            assertNotNull(createdAuthor.getAuthorId());
            assertEquals("Jane Smith", createdAuthor.getName());
            assertTrue(realAuthorDao.searchAuthorByName("Jane Smith").isPresent());
        }

        @Test
        void createNewAuthor_AuthorWithTheSameNameAlreadyExists_IntegrationTest() {
            CreateNewAuthor newAuthor = new CreateNewAuthor("Jane Smith", "Author bio");
            realAuthorService.createNewAuthor(newAuthor);
            assertThrows(IllegalArgumentException.class, () -> realAuthorService.createNewAuthor(newAuthor));
        }

        @Test
        void searchAuthorByName_IntegrationTest() {
            String name = "Unique Author";
            Author author = new Author(name, "Author bio");
            realAuthorDao.addAuthor(author);

            Optional<Author> result = realAuthorService.getAuthorByName(name);

            assertTrue(result.isPresent());
            assertEquals(name, result.get().getName());
        }

        @Test
        void searchAuthorById_IntegrationTest() {
            // Create the Author object
            Author author = new Author("Author Name", "Author bio");

            // Persist the Author
            realAuthorDao.addAuthor(author);

            // Retrieve the persisted Author by its name to obtain the generated ID
            Optional<Author> persistedAuthor = realAuthorDao.searchAuthorByName("Author Name");

            assertTrue(persistedAuthor.isPresent());

            // Use the retrieved Author's ID for the test
            Optional<Author> result = realAuthorService.findAuthorById(persistedAuthor.get().getAuthorId());

            // Assertions
            assertTrue(result.isPresent());
            assertEquals("Author Name", result.get().getName());
            assertEquals("Author bio", result.get().getBio());
            assertEquals(persistedAuthor.get().getAuthorId(), result.get().getAuthorId());
        }

        @Test
        public void testGetPaginatedAuthors() throws Exception {
            mockMvc.perform(get("/authors/paginated"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[0].name").value("Dante Alighieri"))
                    .andExpect(jsonPath("$.data[1].name").value("F. Scott Fitzgerald"))
                    .andExpect(jsonPath("$.data[2].name").value("George Orwell"));
        }

        @Test
        public void testSearchAuthors() throws Exception {
            mockMvc.perform(get("/authors/search?query=J"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[0].name").value("J.D. Salinger"))
                    .andExpect(jsonPath("$.data[1].name").value("J.R.R. Tolkien"))
                    .andExpect(jsonPath("$.data[2].name").value("Jane Austen"));
        }
        @AfterEach
        void tearDown() {
            authorRepository.deleteAll();
        }
    }
}
