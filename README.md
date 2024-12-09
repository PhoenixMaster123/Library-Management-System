# Library Management System

The Library Management System is a Java-based backend application that facilitates the management of books, authors, and transactions for a library. It provides functionalities to manage book catalogs, register customers, process borrowing and returning transactions, and view borrowing history.

## Features

- **Book Management**
  - Add, update, and delete books.
  - Search for books by title, author, or ISBN.

- **Author Management**
  - Manage authors and their associated books.

- **Customer Management**
  - Register customers.
  - View customer borrowing history.

- **Transaction Management**
  - Borrow and return books.
  - Maintain transaction history for each customer.

## Technologies Used

- **Java**: Core programming language.
- **Spring Boot**: Backend framework.
- **JPA/Hibernate**: ORM for database interactions.
- **MYSQL Database**: In-memory database for development and testing.
- **Apache Maven**: Dependency management and build tool.
- **Docker**: Containerization for deployment.
- **JUnit**: Testing framework for unit and integration tests.

## Project Structure

## Setup Instructions

### Prerequisites

- JDK 21
- Apache Maven 3.9.9
- Docker (optional for containerized deployment)

### Installation Steps

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-username/LibraryManagementSystem.git
   cd LibraryManagementSystem
2. **Build the Application**
   ```bash
   mvn clean install
3. **Run the Application**
   ```bash
   mvn spring-boot:run
**API Endpoints**

**Book Endpoints**
GET /books - Retrieve all books.
POST /books - Add a new book.
PUT /books/{id} - Update book details.
DELETE /books/{id} - Delete a book.

**Author Endpoints**
GET /authors - Retrieve all authors.
POST /authors - Add a new author.

**Customer Endpoints**
GET /customers - Retrieve all customers.
POST /customers - Register a new customer.

**Transaction Endpoints**
POST /transactions/borrow - Borrow a book.
POST /transactions/return - Return a book.
