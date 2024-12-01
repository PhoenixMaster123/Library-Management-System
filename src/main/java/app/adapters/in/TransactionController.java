package app.adapters.in;

import app.adapters.in.dto.CreateNewTransaktion;
import app.domain.models.Transaction;
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

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(produces = "application/single-book-response+json;version=1")
    public ResponseEntity<Transaction> createNewTransaction(@RequestBody CreateNewTransaktion newTransaktion) {

        Transaction transaction = transactionService.createNewTransaction(newTransaktion);

        return ResponseEntity.ok(transaction);
    }
    @PostMapping("/returnBook/{bookId}")
    public ResponseEntity<String> returnBook(@RequestParam("bookId") UUID bookId) {
        String transaction_id=transactionService.returnBook(bookId);
        return new ResponseEntity<>(
                "Your Transaction was Successful here is your Txn id:"+transaction_id, HttpStatus.OK);

    }
   /* @PostMapping("/returnBook")
    public ResponseEntity<TransactionResponse> returnBook(@RequestParam UUID bookId) {
        String transactionId = transactionService.returnBook(bookId);
        TransactionResponse response = new TransactionResponse();
        response.setMessage("Transaction successful.");
        response.setTransactionId(UUID.fromString(transactionId));

        return ResponseEntity.ok(response);
    }
    */
    @PostMapping("/borrowBook")
    public ResponseEntity<String> borrowBook(
            @RequestParam UUID customerId,
            @RequestParam UUID bookId) {
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
