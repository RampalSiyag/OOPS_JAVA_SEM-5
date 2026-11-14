# Library Management System

A Spring Boot web application for running a small library: manage books and members, issue and return books, and track overdue loans and fines.

**Stack:** Java 17+, Spring Boot 3.3, Spring MVC, Spring Data JPA (Hibernate), Thymeleaf, Bean Validation, H2 (default) or MySQL, JUnit 5.

## Features

- **Dashboard** with live counts (titles, copies on shelf, members, books on loan, overdue) and a list of overdue books.
- **Books**: add, edit, delete, search by title / author / ISBN / category. Unique ISBN enforced.
- **Members**: register, edit, delete, search. Unique email enforced.
- **Loans**: issue a book, mark it returned, filter by *On loan / Overdue / All history*.
- **Library rules** (see `LoanService`):
  - Loan period is 14 days.
  - Late return fine is ₹5 per day.
  - A member can hold at most 3 books at a time.
  - A member with an overdue book cannot borrow another.
  - A book cannot be issued when no copies are available, or twice to the same member.
  - Books and members with loan history cannot be deleted.
- **Sample data** is loaded automatically the first time the app starts.

## Run it

Requirements: JDK 17 or newer and Maven 3.6+.

```bash
mvn spring-boot:run
```

Then open <http://localhost:8080>.

The default database is a file-based H2 database stored in `./data/`, so nothing needs installing and your data survives restarts. To inspect it, open <http://localhost:8080/h2-console> with JDBC URL `jdbc:h2:file:./data/librarydb`, user `sa`, empty password.

To start with a clean database, stop the app and delete the `data/` folder.

### Use MySQL instead

1. Make sure MySQL is running on `localhost:3306`.
2. Start the app with the `mysql` profile. The `librarydb` database is created automatically.

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

Connection settings are in `src/main/resources/application-mysql.properties`.

### Build a jar

```bash
mvn clean package
java -jar target/library-management-1.0.0.jar
```

## Tests

```bash
mvn test
```

`LoanServiceTest` covers issuing, availability, the 3-book limit, the overdue block, fines and double returns, using an in-memory H2 database.

## Project layout

```
src/main/java/com/library
├── LibraryApplication.java      entry point
├── config/DataLoader.java       sample data on first run
├── controller/                  Dashboard, Book, Member, Loan (Spring MVC)
├── service/                     business rules (BookService, MemberService, LoanService)
├── repository/                  Spring Data JPA repositories
├── model/                       JPA entities: Book, Member, Loan
└── exception/BusinessRuleException.java

src/main/resources
├── application.properties       H2 configuration (default)
├── application-mysql.properties MySQL profile
├── templates/                   Thymeleaf pages
└── static/css/style.css
```

## Data model

```
Book (id, title, author, isbn*, category, totalCopies, availableCopies)
Member (id, name, email*, phone, joinedOn)
Loan (id, book_id -> Book, member_id -> Member, issueDate, dueDate, returnDate, fine)
                                                         * = unique
```

## Ideas to extend it

- Login with Spring Security (librarian / member roles).
- Book reservations and a waiting list.
- Pagination and CSV export of reports.
- A REST API layer (`@RestController`) alongside the web pages.
