package org.example.todo_application.exception;

public class DeadlineCannotBeInPastException extends RuntimeException {
    public DeadlineCannotBeInPastException(String message) {
        super(message);
    }
}
