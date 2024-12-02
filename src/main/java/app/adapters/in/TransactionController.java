package app.adapters.in;

import app.adapters.in.dto.CreateNewTransaktion;
import app.adapters.in.dto.TransactionResponse;
import app.domain.models.Transaction;
import app.domain.services.BookService;
import app.domain.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final BookService bookService;

    @Autowired
    public TransactionController(TransactionService transactionService, BookService bookService) {
        this.transactionService = transactionService;
        this.bookService = bookService;
    }

    @PostMapping(produces = "application/single-book-response+json;version=1")
    public ResponseEntity<Transaction> createNewTransaction(@RequestBody CreateNewTransaktion newTransaktion) {
        try {
            // Create a new transaction
            Transaction transaction = transactionService.createNewTransaction(newTransaktion);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            // Handle the case when the book is not available
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // TODO Not Working
    @PostMapping("/returnBook/{bookId}")
    public ResponseEntity<TransactionResponse> returnBook(@PathVariable UUID bookId) {
        try {
            if (bookService.searchById(bookId).isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new TransactionResponse("Book not found.", null));
            }

            String transactionId = transactionService.returnBook(bookId);
            return ResponseEntity.ok(new TransactionResponse("Transaction successful.", UUID.fromString(transactionId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new TransactionResponse("Failed to return book: " + e.getMessage(), null));
        }
    }

    // This Works:
    @PostMapping("/borrowBook/{customerId}/{bookId}")
    public ResponseEntity<String> borrowBook(
            @PathVariable UUID customerId,
            @PathVariable UUID bookId) {
        try {
            transactionService.borrowBook(customerId, bookId);
            return ResponseEntity.ok("Book borrowed successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to borrow book: " + e.getMessage());
        }
    }

    @GetMapping("/history/{customerId}")
    public ResponseEntity<List<Transaction>> viewBorrowingHistory(@PathVariable UUID customerId) {
        List<Transaction> transactions = transactionService.viewBorrowingHistory(customerId);
        return ResponseEntity.ok(transactions);
    }
}
