# Todo Application

## Project Overview
This is a simple Todo application built with Java and Spring Boot.
The app allows users to create, update, delete, and view a list of todo items. Each todo item can have a name, deadline, priority, and completion status.

## Technologies Used
- **Java 17** - The programming language used for the application.
- **Spring Boot** - Framework for building Java-based applications.
- **Thymeleaf** - Server-side template engine used for rendering HTML views.
- **Spring Data JPA** - Provides an easy way to integrate with a relational database.
- **H2 Database** (for testing) / **MySQL** - Used to store and retrieve todo items.
- **Maven** - For project management and dependency management.
- **Docker & Docker Compose** - Used to containerize the application and manage multi-container setups including the MySQL database.

## Features
- **Create, Read, Update, Delete (CRUD) Operations**: Manage todo items with standard CRUD operations.
- **Task Completion Status**: Mark tasks as finished or not finished.
- **Priority Levels**: Set priority levels (e.g., Low, Medium, High, or No priority) for each todo item.
- **Deadline**: Option to add/change/clear deadline for each task.
- **Error Handling**: Custom error page for specific errors such as deadline in the past, while inline messages handle other validation issues (like required field for todo name).
- **Thymeleaf Frontend**: User interface (UI) built with Thymeleaf. Todo items have inline editing.

## Architecture
The application is structured into several layers:
- **Controller**: Handles HTTP requests, responses, and validations. There are two controllers:
    - `MvcController` for handling frontend interactions via Thymeleaf.
    - `RestController` (primarily for Postman testing and potential future frontend) for API interactions.
- **Service Layer**: Contains business logic for managing todo items, focusing on CRUD operations.
- **Repository Layer**: Uses Spring Data JPA to handle database interactions.
- **DTOs (Data Transfer Objects)**: Used for transferring data between the layers, especially for validation and simplifying the data sent to the frontend.
    - `TodoSaveDto`: Used to handle data for creating, with validation rules.
    - `TodoFrontendDto`: Used to format todo data for display on the frontend in a user-friendly format.

## Database Configuration:
By default, the application uses MySQL.
Update application.properties with your MySQL credentials (or use Environment variables).
For testing, it uses H2 in-memory database

## Docker Configuration:
The project includes Docker to simplify deployment and setup:
- **Dockerfile**: Used to containerize the Java Spring application.
- **Docker Compose**: Manages multi-container deployment, including the application and a MySQL database instance.

## Tests
The project includes both integration tests and unit tests.
Both Types of tests are used for service layer and controller layer.

## Future improvements
**Frontend with React:** Potentially integrate a modern frontend like React to replace Thymeleaf.

**Data Validation and Constraints:** Currently, the name field (the todo's name) is required at the DTO level only.
This requirement is enforced in TodoSaveDto through the `@NotBlank and` and `@Size(max = 100)` annotations, ensuring the name cannot be empty or exceed 100 characters. However, at the database level in the Todo entity, name can be null or blank.
To further strengthen data integrity, we could add validation constraints at the database level in the Todo entity, making the name field non-nullable and enforcing a maximum length constraint directly in the database.
This change would ensure that all layers—both application and database—uniformly enforce the same validation rules.
Validation logic is currently managed at the controller level, ensuring that the name field meets requirements before interacting with the service layer.