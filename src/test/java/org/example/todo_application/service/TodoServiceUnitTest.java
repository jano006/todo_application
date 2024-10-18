package org.example.todo_application.service;


import org.example.todo_application.dto.TodoSaveDto;
import org.example.todo_application.entity.Priority;
import org.example.todo_application.entity.Todo;
import org.example.todo_application.exception.DeadlineCannotBeInPastException;
import org.example.todo_application.mapper.TodoMapper;
import org.example.todo_application.repository.TodoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TodoServiceUnitTest {

    @Mock
    private TodoRepository todoRepository;
    @Mock
    private TodoMapper todoMapper;
    @InjectMocks
    private TodoService todoService;

    // Current date is stored in a variable to ensure consistent date comparisons
    // throughout the tests, avoiding potential timing issues if the test runs around
    // midnight and `LocalDate.now()` is called multiple times.
    LocalDate today = LocalDate.now();

    @Test
    public void testSaveValidTodo() {
        // Arrange
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .deadline(today.plusDays(1))
                .priority(Priority.LOW)
                .build();

        Todo savedTodo = Todo.builder()
                .todoId(1L)
                .name(todoSaveDto.getName())
                .deadline(todoSaveDto.getDeadline())
                .priority(todoSaveDto.getPriority())
                .build();

        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

        // Act
        Todo result = todoService.saveTodo(todoSaveDto);

        // Assert
        Assertions.assertNotNull(result.getTodoId());
        Assertions.assertEquals("Example todo", result.getName());
        Assertions.assertFalse(result.isDone());
        Assertions.assertEquals(today.plusDays(1), result.getDeadline());
        Assertions.assertFalse(savedTodo.isDone());
        verify(todoRepository, times(1)).save(any(Todo.class));
    }

    @Test
    public void testSaveTodoWithInvalidDate() {
        // Arrange
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Task 1")
                .deadline(today.minusDays(1))
                .priority(Priority.LOW)
                .build();

        // Act & Assert
        Exception exception = Assertions.assertThrows(DeadlineCannotBeInPastException.class, () -> {
            todoService.saveTodo(todoSaveDto);
        });
        Assertions.assertEquals("Deadline cannot be in past", exception.getMessage());
        // optional but good and practical
        verify(todoRepository, times(0)).save(any(Todo.class));
    }

    @Test
    public void testUpdateTodoNameWithValidId() {
        // Arrange
        Todo existingTodo = Todo.builder()
                .todoId(1L)
                .name("Example todo")
                .build();

        when(todoRepository.findById(1L)).thenReturn(Optional.of(existingTodo));
        when(todoRepository.save(existingTodo)).thenReturn(existingTodo);

        // Act
        Todo updatedTodo = todoService.updateTodoName(existingTodo.getTodoId(), "New todo");

        // Assert
        Assertions.assertEquals("New todo", updatedTodo.getName());
        verify(todoRepository, times(1)).save(existingTodo);

    }

    @Test
    public void testUpdateTodoNameWithInvalidId() {
        // Arrange
        long nonExistentTodoId = 99L;
        when(todoRepository.findById(nonExistentTodoId)).thenReturn(Optional.empty());

        // Act and assert
        Exception exception = Assertions.assertThrows(RuntimeException.class,
                () -> todoService.updateTodoName(nonExistentTodoId, "New todo"));

        // Assertions
        Assertions.assertEquals("Todo not found", exception.getMessage());
        verify(todoRepository, times(0)).save(any(Todo.class));
    }

    @Test
    public void testChangeTodoIsDoneStatusWhenInitiallyFalse() {
        // Arrange
        Todo todo = Todo.builder()
                .todoId(1L)
                .name("Example todo")
                .isDone(false)
                .build();

        when(todoRepository.findById(todo.getTodoId())).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        // Act
        Todo updatedTodo = todoService.changeTodoIsDoneStatus(todo.getTodoId());

        // Assert
        Assertions.assertTrue(updatedTodo.isDone());
        verify(todoRepository, times(1)).save(todo);
    }

    @Test
    public void testChangeTodoIsDoneStatusWhenInitiallyTrue() {
        // Arrange
        Todo todo = Todo.builder()
                .todoId(1L)
                .name("Example todo")
                .isDone(true)
                .build();

        when(todoRepository.findById(todo.getTodoId())).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        // Act
        Todo updatedTodo = todoService.changeTodoIsDoneStatus(todo.getTodoId());

        // Assert
        Assertions.assertFalse(updatedTodo.isDone());
        verify(todoRepository, times(1)).save(todo);

    }

    @Test
    public void testChangeTodoIsDoneStatusWithInvalidId() {
        // Arrange
        long nonExistentTodoId = 99L;
        when(todoRepository.findById(nonExistentTodoId)).thenReturn(Optional.empty());

        // Act and Assert
        Exception exception = Assertions.assertThrows(Exception.class,
                () -> todoService.changeTodoIsDoneStatus(nonExistentTodoId));
        Assertions.assertEquals("Todo not found", exception.getMessage());
        verify(todoRepository, times(0)).save(any(Todo.class));
    }

    @Test
    public void testUpdateTodoDeadlineWithValidId() {
        // Arrange
        Todo todo = Todo.builder()
                .todoId(1L)
                .name("Example todo")
                .deadline(today.plusDays(3))
                .build();

        when(todoRepository.findById(todo.getTodoId())).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        // Act
        Todo updatedTodo = todoService.updateTodoDeadline(todo.getTodoId(),
                today.plusDays(10));

        // Assert
        Assertions.assertEquals(today.plusDays(10), updatedTodo.getDeadline());
        verify(todoRepository, times(1)).save(todo);
    }

    @Test
    public void testUpdateTodoDeadlineWithInvalidId() {
        // Arrange
        long nonExistentTodoId = 99L;
        when(todoRepository.findById(nonExistentTodoId)).thenReturn(Optional.empty());

        // Act and Assert
        Exception exception = Assertions.assertThrows(Exception.class,
                () -> todoService.updateTodoDeadline(nonExistentTodoId, today.plusDays(10)));
        Assertions.assertEquals("Todo not found", exception.getMessage());
        verify(todoRepository, times(0)).save(any(Todo.class));
    }

    @Test
    public void testUpdateTodoPriorityWithValidId() {
        // Arrange
        Todo todo = Todo.builder()
                .todoId(1L)
                .name("Example todo")
                .priority(Priority.LOW)
                .build();

        when(todoRepository.findById(todo.getTodoId())).thenReturn(Optional.of(todo));
        when(todoRepository.save(todo)).thenReturn(todo);

        // Act
        Todo updatedTodo = todoService.updateTodoPriority(todo.getTodoId(), Priority.MEDIUM);

        // Assert
        Assertions.assertEquals(Priority.MEDIUM, updatedTodo.getPriority());
        verify(todoRepository, times(1)).save(todo);
    }

    @Test
    public void testUpdateTodoPriorityWithInvalidId() {
        // Arrange
        long nonExistentTodoId = 99L;
        when(todoRepository.findById(nonExistentTodoId)).thenReturn(Optional.empty());

        // Act and Assert
        Exception exception = Assertions.assertThrows(Exception.class,
                () -> todoService.updateTodoPriority(nonExistentTodoId, Priority.MEDIUM));
        Assertions.assertEquals("Todo not found", exception.getMessage());
        verify(todoRepository, times(0)).save(any(Todo.class));
    }

    @Test
    public void testDeleteTodoWithValidId() {
        // Arrange
        Todo todo = Todo.builder()
                .todoId(1L)
                .name("Example todo")
                .build();

        when(todoRepository.findById(todo.getTodoId())).thenReturn(Optional.of(todo));

        // Act
        todoService.deleteTodo(todo.getTodoId());

        // Assert
        verify(todoRepository, times(1)).findById(todo.getTodoId());
        verify(todoRepository, times(1)).delete(todo);
    }

    @Test
    public void testDeleteTodoWithInvalidId() {
        // Arrange
        long nonExistentTodoId = 99L;
        when(todoRepository.findById(nonExistentTodoId)).thenReturn(Optional.empty());

        // Act and Assert
        Exception exception = Assertions.assertThrows(Exception.class,
                () -> todoService.deleteTodo(nonExistentTodoId));
        Assertions.assertEquals("Todo not found", exception.getMessage());
        verify(todoRepository, times(1)).findById(nonExistentTodoId);
        verify(todoRepository, times(0)).delete(any(Todo.class));
    }

}
