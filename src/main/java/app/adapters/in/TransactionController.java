package app.adapters.in;

import app.adapters.in.dto.CreateNewTransaktion;
import app.adapters.in.dto.TransactionResponse;
import app.domain.models.Transaction;
import app.domain.services.BookService;
import app.domain.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(value = "/history/{customerId}", produces = "application/paginated-transactions-response+json;version=1")
    @Cacheable(value = "transactions", key = "#customerId.toString() + '-' + #page.orElse(0) + '-' + #size.orElse(10) + '-' + #sortBy.orElse('borrowDate')")
    public ResponseEntity<PagedModel<EntityModel<Transaction>>> viewBorrowingHistory(
            @PathVariable UUID customerId,
            @RequestParam Optional<Integer> page,
            @RequestParam Optional<Integer> size,
            @RequestParam Optional<String> sortBy,
            PagedResourcesAssembler<Transaction> assembler
    ) {
        PageRequest pageable = PageRequest.of(
                page.orElse(0),
                size.orElse(10),
                Sort.Direction.ASC,
                sortBy.orElse("borrowDate")
        );

        Page<Transaction> transactionsPage = transactionService.viewBorrowingHistory(customerId, pageable);

        // Use assembler to construct PagedModel with links
        PagedModel<EntityModel<Transaction>> pagedModel = assembler.toModel(transactionsPage,
                transaction -> EntityModel.of(transaction,
                        linkTo(methodOn(TransactionController.class).getTransactionById(transaction.getTransactionId())).withSelfRel()
                )
        );

        return ResponseEntity.ok(pagedModel);
    }
    @GetMapping("/{transactionId}")
    @Cacheable(value = "transactions", key = "#transactionId")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable UUID transactionId) {
        Optional<Transaction> transactionOpt = transactionService.findById(transactionId);
        return transactionOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
