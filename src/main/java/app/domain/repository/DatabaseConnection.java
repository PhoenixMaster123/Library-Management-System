package app.domain.repository;

import app.persistence.entity.Transaction;

import java.time.LocalDate;
import java.util.List;

public interface DatabaseConnection {

    boolean existsByCustomerIdAndPrivileges(Integer customerId, boolean privileges);
    //List<Transaction> findBorrowingHistory(Integer customerId);
    List<Transaction> findTransactionsByCustomerId(Integer customerId);
    List<Transaction> findByCustomerCustomerId(Integer customerId);

    List<Transaction> findByBookBookId(Integer bookId);

    List<Transaction> findByCustomerCustomerIdAndReturnDateIsNull(Integer customerId);
    List<Transaction> findByDueDateBeforeAndReturnDateIsNull(LocalDate currentDate);

    List<app.persistence.entity.Book> findAllByMultipleCriteria(String title, String authorName, String isbn);

    boolean existsByBookIdAndAvailability(Integer bookId, boolean available);

    app.persistence.entity.Book save(app.persistence.entity.Book book);
}
