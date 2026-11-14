# Student Management REST API

Spring Boot 3 + Spring Data JPA + MySQL.

## Prerequisites
- Java 17+
- Maven 3.8+ (or use your IDE)
- MySQL 8 running locally

## Setup
1. Open `src/main/resources/application.properties` and set your MySQL `username` / `password`.
   The database `student_db` is created automatically (`createDatabaseIfNotExist=true`),
   and the `students` table is created by Hibernate (`ddl-auto=update`).
2. Run:
   ```bash
   mvn spring-boot:run
   ```
   The API starts on `http://localhost:8080`.

## Endpoints
| Method | Endpoint | Description | Success | Errors |
|--------|----------|-------------|---------|--------|
| POST | `/api/students` | Add a new student | 201 | 400 (validation), 409 (duplicate email) |
| GET | `/api/students` | Get all students | 200 | |
| GET | `/api/students/{id}` | Get student by ID | 200 | 404 |
| PUT | `/api/students/{id}` | Update student | 200 | 400, 404, 409 |
| DELETE | `/api/students/{id}` | Delete student | 200 | 404 |
| GET | `/api/students/course/{course}` | Get students by course | 200 | |

## Sample request body
```json
{
  "name": "Aarav Sharma",
  "email": "aarav@example.com",
  "course": "Computer Science",
  "age": 20
}
```

## Testing with Postman
Import `Student_Management_API.postman_collection.json` into Postman, run each request,
and take a screenshot of each response for your submission.

## Project structure
```
src/main/java/com/example/studentmanagement
├── StudentManagementApplication.java
├── controller/StudentController.java
├── entity/Student.java
├── exception/ (StudentNotFoundException, DuplicateEmailException, GlobalExceptionHandler)
├── repository/StudentRepository.java
└── service/ (StudentService, StudentServiceImpl)
```
