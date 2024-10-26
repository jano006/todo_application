package org.example.todo_application.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.todo_application.dto.TodoFrontendDto;
import org.example.todo_application.dto.TodoSaveDto;
import org.example.todo_application.entity.Priority;
import org.example.todo_application.service.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * RestController is mainly used for testing purposes with Postman.
 * Currently, there is no frontend using these endpoints, but they are kept for potential future use and testing.
 */

@org.springframework.web.bind.annotation.RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("api/restController")

public class RestController {
    private final TodoService todoService;

    @PostMapping("/createTodo")
    public ResponseEntity<String> createTodo(@Valid @RequestBody TodoSaveDto todoSaveDto) {
        todoService.saveTodo(todoSaveDto);
        return new ResponseEntity<>(String.format("Todo: %s was saved", todoSaveDto.getName()), HttpStatus.CREATED);
    }

    @GetMapping("/todos/frontendDto")
    public ResponseEntity<List<TodoFrontendDto>> getFrontendTodoList() {
        try {
            return new ResponseEntity<>(todoService.getFrontedTodoDtoList(), HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/updateName")
    public ResponseEntity<String> updateName(@RequestParam Long todoId,
                                             @RequestParam String newName) {
        todoService.updateTodoName(todoId, newName);
        return new ResponseEntity<>(String.format("Todo renamed to: %s", newName), HttpStatus.OK);
    }

    @PatchMapping("/changeIsDoneStatus")
    public ResponseEntity<String> changeIsDoneStatus(@RequestParam Long todoId) {
        todoService.changeTodoIsDoneStatus(todoId);
        return new ResponseEntity<>("Status was changed", HttpStatus.OK);
    }

    @PatchMapping("/updateDeadline")
    public ResponseEntity<String> updateDeadline(@RequestParam Long todoId,
                                                 @RequestParam LocalDate newLocalDate) {
        todoService.updateTodoDeadline(todoId, newLocalDate);
        String message = (newLocalDate == null) ? "Deadline is cleared" : String.format("Deadline adjusted to: %s", newLocalDate);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PatchMapping("/updatePriority")
    public ResponseEntity<String> updatePriority(@RequestParam Long todoId,
                                                 @RequestParam Priority priority) {
        todoService.updateTodoPriority(todoId, priority);
        String message = (priority == null) ? "Priority is cleared" : String.format("Priority adjusted to: %s", priority);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @DeleteMapping("/deleteTodo")
    public ResponseEntity<String> deleteTodo(@RequestParam Long todoId) {
        todoService.deleteTodo(todoId);
        return new ResponseEntity<>("Todo was deleted", HttpStatus.OK);

    }
}
