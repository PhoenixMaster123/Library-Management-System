# Library Management System

The Library Management System is a Java-based backend application that facilitates the management of books, authors, and transactions for a library. It provides functionalities to manage book catalogs, register customers, process borrowing and returning transactions, and view borrowing history.

## Technologies and Dependencies Used

- **Java**: Core programming language.
- **Spring Boot** used to build hassle-free web applications and write REST APIs.
- **Spring Data JPA (Hibernate)** used to reduce the time of writing hardcoded SQL queries and instead allows writing much more readable and scalable code.
- **Spring Security** used for Authentication and Authorizations.
- **MYSQL Database**: In-memory database for development and testing.
- **Apache Maven**: Dependency management and build tool.
- **Docker**: Containerization for deployment.
- **JUnit**: Testing framework for unit and integration tests.
- **Lombok** reduces the time of writing Java boilerplate code.
- **Cashing Strategy**
- **Hypermedia Principle**

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
