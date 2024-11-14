package app.persistence.adapter;

import app.domain.models.Book;
import app.domain.models.Customer;
import app.domain.repository.DatabaseConnection;
import app.persistence.entity.Transaction;

import java.time.LocalDate;
import java.util.List;

//This is for the Implementation of the database queries

public class DBConnector implements DatabaseConnection {
    @Override
    public void addBook(Book book) {

    }

    @Override
    public void updateBook(Book book) {

    }

    @Override
    public void deleteBook(Book book) {

    }

    @Override
    public void searchBookbyID(Integer id) {

    }

    @Override
    public void searchBookByTitle(String title) {

    }

    @Override
    public void searchBookByAuthor(String author) {

    }

    @Override
    public void isbn(String isbn) {

    }

    @Override
    public void addTransaction(Transaction transaction) {

    }

    @Override
    public List<Transaction> getTransactionsforBook(Book book) {
        return List.of();
    }

    @Override
    public List<Transaction> getTransactionsforCustomer(Customer customer) {
        return List.of();
    }

    @Override
    public void addCustomer(Customer customer) {

    }

    @Override
    public List<Customer> getCustomer(Integer id) {
        return List.of();
    }

    @Override
    public void addPrivaliges(Customer customer) {

    }

    // Alle Datenbank Connections
}
