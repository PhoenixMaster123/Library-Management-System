package app.persistence.adapter;

import app.domain.repository.DatabaseConnection;
import app.persistence.entity.Book;
import app.persistence.entity.Transaction;

import java.time.LocalDate;
import java.util.List;

//This is for the Implementation of the database queries

public class DBConnector implements DatabaseConnection {
    @Override
    public boolean existsByCustomerIdAndPrivileges(Integer customerId, boolean privileges) {
        return false;
    }

    @Override
    public List<Transaction> findTransactionsByCustomerId(Integer customerId) {
        return List.of();
    }

    @Override
    public List<Transaction> findByCustomerCustomerId(Integer customerId) {
        return List.of();
    }

    @Override
    public List<Transaction> findByBookBookId(Integer bookId) {
        return List.of();
    }

    @Override
    public List<Transaction> findByCustomerCustomerIdAndReturnDateIsNull(Integer customerId) {
        return List.of();
    }

    @Override
    public List<Transaction> findByDueDateBeforeAndReturnDateIsNull(LocalDate currentDate) {
        return List.of();
    }

    @Override
    public List<Book> findAllByMultipleCriteria(String title, String authorName, String isbn) {
        return List.of();
    }

    @Override
    public boolean existsByBookIdAndAvailability(Integer bookId, boolean available) {
        return false;
    }

    @Override
    public Book save(Book book) {
        return null;
    }
    // Alle Datenbank Connections
}
