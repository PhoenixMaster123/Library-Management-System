package app.infrastructure.config;

import app.adapters.in.dto.CreateNewAuthor;
import app.adapters.in.dto.CreateNewBook;
import app.adapters.in.dto.CreateNewCustomer;
import app.adapters.out.MySQL.repositories.BookRepository;
import app.adapters.out.MySQL.repositories.CustomRepository;
import app.domain.services.BookService;
import app.domain.services.CustomerService;
import app.domain.services.TransactionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final BookService bookService;
    private final CustomerService customerService;
    private final TransactionService transactionService;
    private final BookRepository bookRepository;
    private final CustomRepository customRepository;

    public DatabaseSeeder(BookService bookService, CustomerService customerService, TransactionService transactionService, BookRepository bookRepository, CustomRepository customRepository) {
        this.bookService = bookService;
        this.customerService = customerService;
        this.transactionService = transactionService;
        this.bookRepository = bookRepository;
        this.customRepository = customRepository;
    }

    @Override
    public void run(String... args) {
        List<UUID> customerIds = new LinkedList<>();
        List<UUID> bookIds = new LinkedList<>();

        List<CreateNewBook> booksToSeed = List.of(
                new CreateNewBook(
                        "The Great Gatsby",
                        "9780743273565",
                        1925,
                        List.of(new CreateNewAuthor("F. Scott Fitzgerald", "An American novelist."))),
                new CreateNewBook(
                        "To Kill a Mockingbird",
                        "9780061120084",
                        1960,
                        List.of(new CreateNewAuthor("Harper Lee", "An American novelist best known for 'To Kill a Mockingbird'."))),
                new CreateNewBook(
                        "1984",
                        "9780451524935",
                        1949,
                        List.of(new CreateNewAuthor("George Orwell", "British writer and journalist."))),
                new CreateNewBook(
                        "Moby Dick",
                        "9780142437247",
                        1851,
                        List.of(new CreateNewAuthor("Herman Melville", "An American novelist and poet."))),
                new CreateNewBook(
                        "War and Peace",
                        "9780199232765",
                        1869,
                        List.of(new CreateNewAuthor("Leo Tolstoy", "A Russian writer and philosopher."))),
                new CreateNewBook(
                        "Pride and Prejudice",
                        "9780141439518",
                        1813,
                        List.of(new CreateNewAuthor("Jane Austen", "An English novelist known for her romantic fiction."))),
                new CreateNewBook(
                        "The Catcher in the Rye",
                        "9780316769488",
                        1951,
                        List.of(new CreateNewAuthor("J.D. Salinger", "An American writer."))),
                new CreateNewBook(
                        "The Hobbit",
                        "9780261102217",
                        1937,
                        List.of(new CreateNewAuthor("J.R.R. Tolkien", "An English writer, poet, and academic."))),
                new CreateNewBook(
                        "The Divine Comedy",
                        "9780199535645",
                        1320,
                        List.of(new CreateNewAuthor("Dante Alighieri", "An Italian poet, writer, and philosopher."))),
                new CreateNewBook(
                        "The Odyssey",
                        "9780140268867",
                        -800, // Assuming BC era, use negative values.
                        List.of(new CreateNewAuthor("Homer", "Ancient Greek poet.")))
        );

        booksToSeed.forEach(book -> {
            try {
                bookService.createNewBook(book);
                UUID bookId = bookRepository.findBooksByIsbn(book.getIsbn())
                        .orElseThrow(() -> new IllegalStateException("Book not found"))
                        .getBookId();
                bookIds.add(bookId); // Store the ID
                System.out.println("Seeded book: " + book.getTitle());
            } catch (IllegalArgumentException e) {
                System.out.println("Skipping book: " + book.getTitle() + " - " + e.getMessage());
            }
        });
        List<CreateNewCustomer> customersToSeed = List.of(
                new CreateNewCustomer("Alice Smith", "alice.smith@example.com", true),
                new CreateNewCustomer("Bob Johnson", "bob.johnson@example.com", true),
                new CreateNewCustomer("Charlie Brown", "charlie.brown@example.com", true),
                new CreateNewCustomer("Diana Prince", "diana.prince@example.com", true),
                new CreateNewCustomer("Ethan Hunt", "ethan.hunt@example.com", true),
                new CreateNewCustomer("Fiona Gallagher", "fiona.gallagher@example.com", true),
                new CreateNewCustomer("George Bailey", "george.bailey@example.com", true),
                new CreateNewCustomer("Hannah Abbott", "hannah.abbott@example.com", true),
                new CreateNewCustomer("Ivan Drago", "ivan.drago@example.com", true),
                new CreateNewCustomer("Julia Roberts", "julia.roberts@example.com", true)
        );

        customersToSeed.forEach(customer -> {
            try {
                customerService.createNewCustomer(customer);
                UUID customerId = customRepository.findByName(customer.getName())
                        .orElseThrow(() -> new IllegalStateException("Customer not found"))
                        .getCustomerId();
                customerIds.add(customerId);
                System.out.println("Seeded customer: " + customer.getName());
            } catch (Exception e) {
                System.out.println("Skipping customer: " + customer.getName() + " - " + e.getMessage());
            }
        });
        // Seed transactions
        customerIds.forEach(customerId -> {
            bookIds.forEach(bookId -> {
                try {
                    // Generate a random borrow date (e.g., within the last 30 days)
                    LocalDate borrowDate = LocalDate.now().minusDays(ThreadLocalRandom.current().nextInt(1, 31));
                    // Return date is borrow date + a random number of days (7 to 21 days)
                    LocalDate returnDate = borrowDate.plusDays(ThreadLocalRandom.current().nextInt(7, 21));

                    transactionService.borrowBookWithDates(customerId, bookId, borrowDate); // Borrow book with dates
                    System.out.println("Borrowed book: " + bookId + " by customer: " + customerId + " on " + borrowDate);

                    // Simulate book return
                    transactionService.returnBookWithDates(bookId, returnDate); // Return book
                    System.out.println("Returned book: " + bookId + " by customer: " + customerId + " on " + returnDate);
                } catch (Exception e) {
                    System.out.println("Skipping transaction for customer " + customerId + " and book " + bookId + " - " + e.getMessage());
                }
            });
        });

    }
}
