package app.adapters.in;

import app.adapters.in.dto.CreateNewTransaktion;
import app.adapters.in.dto.TransactionResponse;
import app.domain.models.Transaction;
import app.domain.services.BookService;
import app.domain.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

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

    @PostMapping(produces = "application/single-transaction-response+json;version=1")
    public ResponseEntity<Transaction> createNewTransaction(@Valid @RequestBody CreateNewTransaktion newTransaktion) {
        try {
            // Create a new transaction
            Transaction transaction = transactionService.createNewTransaction(newTransaktion);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            // Handle the case when the book is not available
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping(value = "/returnBook/{bookId}", produces = "application/transaction-response+json;version=1")
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

    @PostMapping(value = "/borrowBook/{customerId}/{bookId}", produces = "application/transaction-response+json;version=1")
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

    @GetMapping(value = "/history/{customerId}", produces = "application/paginated-transactions-response+json;version=1")
    public ResponseEntity<Map<String, Object>> viewBorrowingHistory(
            @PathVariable UUID customerId,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam Optional<String> sortBy
    ) {
        int currentPage = page.orElse(0);
        int pageSize = size.orElse(5);
        String sortField = sortBy.orElse("borrowDate");

        PageRequest pageable = PageRequest.of(currentPage, pageSize, Sort.Direction.ASC, sortField);
        Page<Transaction> transactionsPage = transactionService.viewBorrowingHistory(customerId, pageable);

        // Add pagination links to headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("self", "<" + linkTo(methodOn(TransactionController.class)
                .viewBorrowingHistory(customerId, Optional.of(currentPage), Optional.of(pageSize), Optional.of(sortField))).toUri() + ">; rel=\"self\"");

        if (transactionsPage.hasPrevious()) {
            headers.add("prev", "<" + linkTo(methodOn(TransactionController.class)
                    .viewBorrowingHistory(customerId, Optional.of(currentPage - 1), Optional.of(pageSize), Optional.of(sortField))).toUri() + ">; rel=\"prev\"");
        }
        if (transactionsPage.hasNext()) {
            headers.add("next", "<" + linkTo(methodOn(TransactionController.class)
                    .viewBorrowingHistory(customerId, Optional.of(currentPage + 1), Optional.of(pageSize), Optional.of(sortField))).toUri() + ">; rel=\"next\"");
        }

        // Build response body
        if (transactionsPage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No transactions found for this customer"));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", transactionsPage.getContent());
        response.put("totalPages", transactionsPage.getTotalPages());
        response.put("currentPage", transactionsPage.getNumber());
        response.put("totalItems", transactionsPage.getTotalElements());

        // Return the response with headers
        return ResponseEntity.ok().headers(headers).body(response);
    }

    @GetMapping(value = "/{id}", produces = "application/single-transaction-response+json;version=1")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable UUID id) {
        Optional<Transaction> transactionOpt = transactionService.findById(id);
        return transactionOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
