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

Here’s the updated README with the necessary points added:

markdown
Copy code
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
- **H2 Database**: In-memory database for development and testing.
- **Apache Maven**: Dependency management and build tool.
- **Docker**: Containerization for deployment.
- **JUnit**: Testing framework for unit and integration tests.

## Project Structure

LibraryManagementSystem/ ├── src/main/java/com/library │ ├── Application.java # Main entry point of the application │ ├── entity/ │ │ ├── Book.java # Entity class for books │ │ ├── Author.java # Entity class for authors │ │ ├── Customer.java # Entity class for customers │ │ └── Transaction.java # Entity class for transactions │ ├── repository/ │ │ ├── BookRepository.java # Repository interface for books │ │ ├── AuthorRepository.java # Repository interface for authors │ │ └── TransactionRepository.java # Repository for transactions │ ├── service/ # Business logic and service layer │ └── controller/ # REST API controllers ├── src/main/resources/ │ ├── application.yml # Application configuration ├── src/test/java/com/library/ # Unit and integration tests ├── Dockerfile # Docker configuration for containerization ├── pom.xml # Maven dependencies and build configuration

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
```bash
mvn clean install
