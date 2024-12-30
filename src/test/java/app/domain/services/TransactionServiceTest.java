package app.domain.services;

import app.adapters.out.MySQL.repositories.BookRepository;
import app.adapters.out.MySQL.repositories.CustomerRepository;
import app.adapters.out.MySQL.repositories.TransactionRepository;
import app.domain.models.Book;
import app.domain.models.Customer;
import app.domain.models.Transaction;
import app.domain.port.BookDao;
import app.domain.port.CustomerDao;
import app.domain.port.TransactionDao;
import app.adapters.in.dto.CreateNewTransaktion;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Nested
    class UnitTests {

        @Mock
        private TransactionDao transactionDao;
        @Mock
        private BookDao bookDao;
        @Mock
        private CustomerDao customerDao;
        @InjectMocks
        private TransactionService transactionService;

        @BeforeEach
        void setUp() {
            transactionService = new TransactionService(transactionDao, bookDao, customerDao);
        }

        @Test
        void testCreateNewTransaction_ValidInput_Success() {
            // Given
            CreateNewTransaktion createNewTransaktion = new CreateNewTransaktion(
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(10),
                    UUID.randomUUID(),
                    UUID.randomUUID()
            );

            Customer expectedCustomer = new Customer(UUID.randomUUID(), "John Doe", "john.doe@example.com", true);
            Book expectedBook = new Book(UUID.randomUUID(), "Clean Code", "Robert C. Martin", 2008, true, null);

            when(customerDao.getCustomer(createNewTransaktion.getCustomerId())).thenReturn(Optional.of(expectedCustomer));
            when(bookDao.searchBookById(createNewTransaktion.getBookId())).thenReturn(Optional.of(expectedBook));

            // When
            Transaction transaction = transactionService.createNewTransaction(createNewTransaktion);

            // Then
            verify(transactionDao).addTransaction(transaction);
            assertThat(transaction.getCustomer()).isEqualTo(expectedCustomer);
            assertThat(transaction.getBook()).isEqualTo(expectedBook);
        }

        @Test
        void testCreateNewTransaction_BorrowDateAfterDueDate_ThrowsException() {
            // Given
            CreateNewTransaktion createNewTransaktion = new CreateNewTransaktion(
                    LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(5),
                    UUID.randomUUID(),
                    UUID.randomUUID()
            );

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> transactionService.createNewTransaction(createNewTransaktion));
            verifyNoInteractions(transactionDao);
        }

        @Test
        void testCreateNewTransaction_DueDateBeforeToday_ThrowsException() {
            // Given
            CreateNewTransaktion createNewTransaktion = new CreateNewTransaktion(
                    LocalDate.now().minusDays(2),
                    LocalDate.now().minusDays(1),
                    UUID.randomUUID(),
                    UUID.randomUUID()
            );

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> transactionService.createNewTransaction(createNewTransaktion));
            verifyNoInteractions(transactionDao);
        }

        @Test
        void testCreateNewTransaction_CustomerNotFound_ThrowsException() {
            // Given
            CreateNewTransaktion createNewTransaktion = new CreateNewTransaktion(
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(10),
                    UUID.randomUUID(),
                    UUID.randomUUID()
            );
            when(customerDao.getCustomer(createNewTransaktion.getCustomerId())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(EntityNotFoundException.class, () -> transactionService.createNewTransaction(createNewTransaktion));
            verifyNoInteractions(transactionDao);
        }

        @Test
        void testCreateNewTransaction_BookNotFound_ThrowsException() {
            // Given
            CreateNewTransaktion createNewTransaktion = new CreateNewTransaktion(
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(10),
                    UUID.randomUUID(),
                    UUID.randomUUID()
            );
            when(customerDao.getCustomer(createNewTransaktion.getCustomerId())).thenReturn(Optional.of(new Customer()));
            when(bookDao.searchBookById(createNewTransaktion.getBookId())).thenReturn(Optional.empty());

            // When & Then
            assertThrows(EntityNotFoundException.class, () -> transactionService.createNewTransaction(createNewTransaktion));
            verifyNoInteractions(transactionDao);
        }

        @Test
        void testReturnBook_NoTransactionFound_ThrowsException() {
            // Given
            UUID bookId = UUID.randomUUID();
            Book book = new Book(bookId, null, null, 0, false, null);

            // Use an ArgumentMatcher to match any Book object with the same ID
            when(transactionDao.getTransactionsForBook(argThat(b ->
                    b.getBookId().equals(bookId)))).thenReturn(Collections.emptyList());

            // When & Then
            assertThrows(EntityNotFoundException.class, () -> transactionService.returnBook(bookId));

            // Verify that no interactions occurred with bookDao
            verifyNoInteractions(bookDao);
        }


        @Test
        void testReturnBook_Success() {
            // Given
            UUID bookId = UUID.randomUUID();
            Customer customer = new Customer(UUID.randomUUID(), "John Doe", "john.doe@example.com", true);
            Book book = new Book(bookId, "Clean Code", "Robert C. Martin", 2008, false, null); // Book is already borrowed
            Transaction transaction = new Transaction(LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), customer, book);
            when(transactionDao.getTransactionsForBook(any(Book.class))).thenReturn(List.of(transaction));

            // When
            String transactionId = transactionService.returnBook(bookId);

            // Then
            assertThat(transactionId).isEqualTo(transaction.getTransactionId().toString());
            assertThat(transaction.getReturnDate()).isEqualTo(LocalDate.now()); // Assert directly on the property
            assertThat(transaction.getBook().isAvailable()).isTrue(); // Assert on the nested Book object
            verify(transactionDao).updateTransaction(transaction);
            verify(bookDao).updateBook(bookId, transaction.getBook());
        }

        @Test
        void testBorrowBook_BookNotFound_ThrowsException() {
            // Given
            UUID customerId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            when(bookDao.searchBookById(bookId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class, () -> transactionService.borrowBook(customerId, bookId));

            // Verify no interactions with customerDao and transactionDao
            verifyNoInteractions(customerDao, transactionDao);

            // Verify interaction with bookDao to confirm it was used for the search
            verify(bookDao).searchBookById(bookId);
        }


        @Test
        void testBorrowBook_CustomerNotFound_ThrowsException() {
            // Given
            UUID customerId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            Book book = new Book(bookId, "Clean Code", "Robert C. Martin", 2008, true, null);
            when(bookDao.searchBookById(bookId)).thenReturn(Optional.of(book));
            when(customerDao.getCustomer(customerId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(RuntimeException.class, () -> transactionService.borrowBook(customerId, bookId));
            verify(bookDao).searchBookById(bookId); // Verify expected interaction
        }

        @Test
        void testBorrowBook_BookNotAvailable_ThrowsException() {
            // Given
            UUID customerId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            Book book = new Book(bookId, "Clean Code", "Robert C. Martin", 2008, false, null); // Book is already borrowed
            when(bookDao.searchBookById(bookId)).thenReturn(Optional.of(book)); // Only stub what's relevant

            // When & Then
            assertThrows(RuntimeException.class, () -> transactionService.borrowBook(customerId, bookId));

            // Verify that bookDao was interacted with
            verify(bookDao).searchBookById(bookId);
        }


        @Test
        void testBorrowBook_CustomerNoPrivileges_ThrowsException() {
            // Given
            UUID customerId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            Book book = new Book(bookId, "Clean Code", "Robert C. Martin", 2008, true, null);
            Customer customer = new Customer(customerId, "John Doe", "john.doe@example.com", false); // Customer has no privileges
            when(bookDao.searchBookById(bookId)).thenReturn(Optional.of(book));
            when(customerDao.getCustomer(customerId)).thenReturn(Optional.of(customer));

            // When & Then
            assertThrows(RuntimeException.class, () -> transactionService.borrowBook(customerId, bookId));
            verify(bookDao).searchBookById(bookId); // Verify expected interaction
        }

        @Test
        void testBorrowBook_Success() {
            // Given
            UUID customerId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            Book book = new Book(bookId, "Clean Code", "Robert C. Martin", 2008, true, null);
            Customer customer = new Customer(customerId, "John Doe", "john.doe@example.com", true);
            when(bookDao.searchBookById(bookId)).thenReturn(Optional.of(book));
            when(customerDao.getCustomer(customerId)).thenReturn(Optional.of(customer));

            // When
            Transaction transaction = transactionService.borrowBook(customerId, bookId);

            // Then
            assertThat(transaction.getCustomer()).isEqualTo(customer);
            assertThat(transaction.getBook()).isEqualTo(book);
            assertThat(transaction.getBorrowDate()).isBetween(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
            assertThat(transaction.getDueDate()).isBetween(LocalDate.now().plusWeeks(2).minusDays(1), LocalDate.now().plusWeeks(2).plusDays(1));

            // Verify interactions with DAOs
            verify(bookDao).searchBookById(bookId);
            verify(customerDao).getCustomer(customerId);
            verify(transactionDao).addTransaction(transaction);

            // Assert on the state of the book
            assertThat(book.isAvailable()).isFalse();
        }

        @Test
        void testViewBorrowingHistory() {
            // Given
            UUID customerId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);
            Page<Transaction> expectedPage = mock(Page.class);
            when(transactionDao.viewBorrowingHistory(customerId, pageable)).thenReturn(expectedPage);

            // When
            Page<Transaction> actualPage = transactionService.viewBorrowingHistory(customerId, pageable);

            // Then
            assertThat(actualPage).isEqualTo(expectedPage);
            verify(transactionDao).viewBorrowingHistory(customerId, pageable);
        }

        @Test
        void testFindById() {
            // Given
            Transaction transaction = new Transaction(LocalDate.now(), LocalDate.now().plusDays(10), new Customer(), new Book());
            when(transactionDao.findTransactionById(transaction.getTransactionId())).thenReturn(Optional.of(transaction));

            // When
            Optional<Transaction> actualTransaction = transactionService.findById(transaction.getTransactionId());

            // Then
            assertThat(actualTransaction).isPresent();
            assertThat(actualTransaction.get()).isEqualTo(transaction);
            verify(transactionDao).findTransactionById(transaction.getTransactionId());
        }

        @Test
        void testFindById_NotFound() {
            // Given
            UUID transactionId = UUID.randomUUID();
            when(transactionDao.findTransactionById(transactionId)).thenReturn(Optional.empty());

            // When
            Optional<Transaction> actualTransaction = transactionService.findById(transactionId);

            // Then
            assertThat(actualTransaction).isEmpty();
            verify(transactionDao).findTransactionById(transactionId);
        }

        @Test
        void testBorrowBookWithDates_BookNotFound_ThrowsException() {
            // Given
            UUID customerId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            LocalDate borrowDate = LocalDate.now().plusDays(1);
            when(bookDao.searchBookById(bookId)).thenReturn(Optional.empty()); // Stub bookDao to return empty

            // When & Then
            assertThrows(IllegalStateException.class, () -> transactionService.borrowBookWithDates(customerId, bookId, borrowDate));

            // Verify no interactions with customerDao and transactionDao
            verifyNoInteractions(customerDao, transactionDao);

            // Verify the expected interaction with bookDao
            verify(bookDao).searchBookById(bookId);
        }


        @Test
        void testBorrowBookWithDates_CustomerNotFound_ThrowsException() {
            // Given
            UUID customerId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            LocalDate borrowDate = LocalDate.now().plusDays(1);
            Book book = new Book(bookId, "Clean Code", "Robert C. Martin", 2008, true, null);
            when(bookDao.searchBookById(bookId)).thenReturn(Optional.of(book));
            when(customerDao.getCustomer(customerId)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalStateException.class, () -> transactionService.borrowBookWithDates(customerId, bookId, borrowDate));
            verify(bookDao).searchBookById(bookId); // Verify expected interaction
        }

        @Test
        void testBorrowBookWithDates_BookNotAvailable_ThrowsException() {
            // Given
            UUID customerId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            LocalDate borrowDate = LocalDate.now().plusDays(1);
            Book book = new Book(bookId, "Clean Code", "Robert C. Martin", 2008, false, null); // Book is already borrowed
            when(bookDao.searchBookById(bookId)).thenReturn(Optional.of(book));

            // When & Then
            assertThrows(IllegalArgumentException.class, () -> transactionService.borrowBookWithDates(customerId, bookId, borrowDate));
            verify(bookDao).searchBookById(bookId); // Verify expected interaction
        }

        @Test
        void testBorrowBookWithDates_Success() {
            // Given
            UUID customerId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            LocalDate borrowDate = LocalDate.now().plusDays(1);

            // Use mock for the Book object
            Book book = mock(Book.class);
            Customer customer = new Customer(customerId, "John Doe", "john.doe@example.com", true);

            when(bookDao.searchBookById(bookId)).thenReturn(Optional.of(book));
            when(customerDao.getCustomer(customerId)).thenReturn(Optional.of(customer));
            when(book.isAvailable()).thenReturn(true); // Set book available

            // When
            transactionService.borrowBookWithDates(customerId, bookId, borrowDate);

            // Then
            verify(bookDao).searchBookById(bookId);
            verify(customerDao).getCustomer(customerId);

            // Verify interaction with mocked book
            verify(book).setAvailable(false);

            // Use captor to verify the transaction details
            ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
            verify(transactionDao).addTransaction(transactionCaptor.capture());
            Transaction capturedTransaction = transactionCaptor.getValue();
            assertThat(capturedTransaction.getBorrowDate()).isEqualTo(borrowDate);
            assertThat(capturedTransaction.getDueDate()).isEqualTo(borrowDate.plusWeeks(2));
            assertThat(capturedTransaction.getCustomer()).isEqualTo(customer);
            assertThat(capturedTransaction.getBook()).isEqualTo(book);
        }


        @Test
        void testReturnBookWithDates_NoTransactionFound_ThrowsException() {
            // Given
            UUID bookId = UUID.randomUUID();
            LocalDate returnDate = LocalDate.now();
            when(transactionDao.getTransactionsForBook(any(Book.class))).thenReturn(Collections.emptyList());

            // When & Then
            assertThrows(EntityNotFoundException.class, () -> transactionService.returnBookWithDates(bookId, returnDate));
            verifyNoInteractions(bookDao); // Assuming bookDao is not interacted with in this scenario
        }

        @Test
        void testReturnBookWithDates_Success() {
            // Given
            UUID bookId = UUID.randomUUID();
            LocalDate returnDate = LocalDate.now();
            Customer customer = new Customer(UUID.randomUUID(), "John Doe", "john.doe@example.com", true);
            Book book = new Book(bookId, "Clean Code", "Robert C. Martin", 2008, false, null);
            Transaction transaction = new Transaction(LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), customer, book);
            when(transactionDao.getTransactionsForBook(any(Book.class))).thenReturn(List.of(transaction));

            // When
            transactionService.returnBookWithDates(bookId, returnDate);

            // Then
            assertThat(transaction.getReturnDate()).isEqualTo(returnDate); // Assert directly on the object
            assertThat(transaction.getBook().isAvailable()).isTrue(); // Assert on the nested Book object
            verify(transactionDao).updateTransaction(transaction);
            verify(bookDao).updateBook(bookId, transaction.getBook());
        }
    }

    @Nested
    @SpringBootTest
    class IntegrationTests {

        @Autowired
        private TransactionService transactionService;
        @Autowired
        private TransactionDao transactionDao;
        @Autowired
        private BookDao bookDao;
        @Autowired
        private CustomerDao customerDao;

        @Autowired
        private TransactionRepository transactionRepository;
        @Autowired
        private BookRepository bookRepository;
        @Autowired
        private CustomerRepository customerRepository;

        @Test
        void testCreateNewTransaction_Integration() {
            // Given
            Customer customer = new Customer(UUID.randomUUID(), "John Doe", "john.doe@example.com", true);
            customerDao.addCustomer(customer); // Save the customer

            // Refresh the customer to ensure it's persisted
            customer = customerDao.getCustomer(customer.getCustomerId()).get();

            Book book = new Book(UUID.randomUUID(), "Clean Code", "Robert C. Martin", 2008, true, LocalDate.now());
            bookDao.addBook(book); // Ensure book is saved before fetching the ID

            CreateNewTransaktion createNewTransaction = new CreateNewTransaktion(
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(10),
                    customer.getCustomerId(),
                    book.getBookId()
            );

            // When
            Transaction transaction = transactionService.createNewTransaction(createNewTransaction);

            // Then
            assertThat(transaction).isNotNull();
            assertThat(transaction.getCustomer()).isNotNull();
            assertThat(transaction.getBook()).isNotNull();
            assertThat(transaction.getCustomer().getCustomerId()).isEqualTo(customer.getCustomerId());
            assertThat(transaction.getBook().getBookId()).isEqualTo(book.getBookId());
            assertThat(transaction.getBorrowDate()).isEqualTo(createNewTransaction.getBorrowDate());
            assertThat(transaction.getDueDate()).isEqualTo(createNewTransaction.getDueDate());
            assertThat(transaction.getTransactionId()).isNotNull(); // Check if the transaction ID was generated
        }

        @Test
        void testReturnBook_Integration() {
            // Given
            Customer customer = new Customer(UUID.randomUUID(), "John Doe", "john.doe@example.com", true);
            customerDao.addCustomer(customer);
            Book book = new Book(UUID.randomUUID(), "Clean Code", "Robert C. Martin", 2008, false, LocalDate.now());
            bookDao.addBook(book);
            Transaction transaction = new Transaction(LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), customer, book);
            transactionDao.addTransaction(transaction);

            // When
            String transactionId = transactionService.returnBook(book.getBookId());

            // Then
            Optional<Transaction> returnedTransaction = transactionDao.findTransactionById(UUID.fromString(transactionId));
            assertThat(returnedTransaction).isPresent();
            assertThat(returnedTransaction.get().getReturnDate()).isNotNull();
            assertThat(bookDao.searchBookById(book.getBookId()).get().isAvailable()).isTrue();
        }

        @Test
        void testBorrowBook_Integration() {
            // Given
            Customer customer = new Customer(UUID.randomUUID(), "John Doe", "john.doe@example.com", true);
            customerDao.addCustomer(customer);

            Book book = new Book(UUID.randomUUID(), "Clean Code", "Robert C. Martin", 2008, true, LocalDate.now());
            bookDao.addBook(book);

            // When
            Transaction transaction = transactionService.borrowBook(customer.getCustomerId(), book.getBookId());

            // Then
            assertThat(transaction.getCustomer().getCustomerId()).isEqualTo(customer.getCustomerId());
            assertThat(transaction.getBook().getBookId()).isEqualTo(book.getBookId());
            assertThat(bookDao.searchBookById(book.getBookId()).get().isAvailable()).isFalse();
        }

        @Test
        void testViewBorrowingHistory_Integration() {
            // Given
            Customer customer = new Customer(UUID.randomUUID(), "John Doe", "john.doe@example.com", true);
            customerDao.addCustomer(customer);
            Book book1 = new Book(UUID.randomUUID(), "Clean Code", "Robert C. Martin", 2008, true, LocalDate.now());
            bookDao.addBook(book1);
            Book book2 = new Book(UUID.randomUUID(), "Design Patterns", "Gang of Four", 1994, true, LocalDate.now());
            bookDao.addBook(book2);

            transactionService.borrowBook(customer.getCustomerId(), book1.getBookId());
            transactionService.borrowBook(customer.getCustomerId(), book2.getBookId());

            Pageable pageable = PageRequest.of(0, 10);

            // When
            Page<Transaction> actualPage = transactionService.viewBorrowingHistory(customer.getCustomerId(), pageable);

            // Then
            assertThat(actualPage.getContent()).hasSize(2);
        }

        @Test
        void testFindById_Integration() {
            // Given
            Customer customer = new Customer(null, "John Doe", "john.doe@example.com", true);
            customerDao.addCustomer(customer);
            Book book = new Book(null, "Clean Code", "Robert C. Martin", 2008, false, LocalDate.now());
            bookDao.addBook(book);
            Transaction transaction = new Transaction(LocalDate.now().minusDays(5), LocalDate.now().plusDays(5), customer, book);
            transactionDao.addTransaction(transaction);

            // When
            Optional<Transaction> actualTransaction = transactionService.findById(transaction.getTransactionId());

            // Then
            assertThat(actualTransaction).isPresent();

            // Compare field-by-field instead of using equals()
            Transaction actual = actualTransaction.get();
            assertThat(actual.getTransactionId()).isEqualTo(transaction.getTransactionId());
            assertThat(actual.getBorrowDate()).isEqualTo(transaction.getBorrowDate());
            assertThat(actual.getDueDate()).isEqualTo(transaction.getDueDate());

            // Compare fields of the customer
            assertThat(actual.getCustomer().getCustomerId()).isEqualTo(transaction.getCustomer().getCustomerId());
            assertThat(actual.getCustomer().getName()).isEqualTo(transaction.getCustomer().getName());
            assertThat(actual.getCustomer().getEmail()).isEqualTo(transaction.getCustomer().getEmail());
            assertThat(actual.getCustomer().isPrivileges()).isEqualTo(transaction.getCustomer().isPrivileges());

            // Compare fields of the book
            assertThat(actual.getBook().getBookId()).isEqualTo(transaction.getBook().getBookId());
            assertThat(actual.getBook().getTitle()).isEqualTo(transaction.getBook().getTitle());
            assertThat(actual.getBook().getAuthors()).isEqualTo(transaction.getBook().getAuthors());
            assertThat(actual.getBook().getPublicationYear()).isEqualTo(transaction.getBook().getPublicationYear());
        }


        @AfterEach
        void tearDown() {
            transactionRepository.deleteAll();
            bookRepository.deleteAll();
            customerRepository.deleteAll();
        }
    }
}