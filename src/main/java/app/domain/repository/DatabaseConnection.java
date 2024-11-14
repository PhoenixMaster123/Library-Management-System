package app.domain.repository;

import app.domain.models.Book;
import app.domain.models.Customer;
import app.persistence.entity.Transaction;


import java.time.LocalDate;
import java.util.List;

public interface DatabaseConnection {
    public void addBook(Book book);
    public void updateBook(Book book);
    public void deleteBook(Book book);//or with ID
    public void searchBookbyID(Integer id );
    public void searchBookByTitle(String title);
    public void searchBookByAuthor(String author);
    public void isbn(String isbn);

    public void addTransaction(Transaction transaction);

    public List<Transaction> getTransactionsforBook(Book book);
    public List<Transaction> getTransactionsforCustomer(Customer customer);
    public void addCustomer(Customer customer);
    public List<Customer> getCustomer(Integer id);
    public void addPrivaliges(Customer customer);



/*
    boolean existsByCustomerIdAndPrivileges(Integer customerId, boolean privileges);
    //List<Transaction> findBorrowingHistory(Integer customerId);
    List<Transaction> findTransactionsByCustomerId(Integer customerId);
    List<Transaction> findByCustomerCustomerId(Integer customerId);

    List<Transaction> findByBookBookId(Integer bookId);

    List<Transaction> findByCustomerCustomerIdAndReturnDateIsNull(Integer customerId);
    List<Transaction> findByDueDateBeforeAndReturnDateIsNull(LocalDate currentDate);

    List<app.persistence.entity.Book> findAllByMultipleCriteria(String title, String authorName, String isbn);

    boolean existsByBookIdAndAvailability(Integer bookId, boolean available);

    app.persistence.entity.Book save(app.persistence.entity.Book book);*/
}
