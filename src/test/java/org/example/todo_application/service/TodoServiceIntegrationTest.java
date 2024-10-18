package org.example.todo_application.service;

import org.example.todo_application.dto.TodoFrontendDto;
import org.example.todo_application.dto.TodoSaveDto;
import org.example.todo_application.entity.Priority;
import org.example.todo_application.entity.Todo;
import org.example.todo_application.exception.DeadlineCannotBeInPastException;
import org.example.todo_application.repository.TodoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class
TodoServiceIntegrationTest {

    @Autowired
    private TodoService todoService;
    @Autowired
    private TodoRepository todoRepository;

    @BeforeEach
    void setup() {
        todoRepository.deleteAll();
    }

    // Current date is stored in a variable to ensure consistent date comparisons
    // throughout the tests, avoiding potential timing issues if the test runs around
    // midnight and `LocalDate.now()` is called multiple times.
    LocalDate today = LocalDate.now();

    @Test
    public void testSaveValidTodo() {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .deadline(today.plusDays(1))
                .priority(Priority.LOW)
                .build();
        Todo todo = todoService.saveTodo(todoSaveDto);

        Assertions.assertNotNull(todo.getTodoId(), "todo id should not be null");
        Assertions.assertEquals("Example todo", todo.getName());
        Assertions.assertEquals(today.plusDays(1), todo.getDeadline());
        Assertions.assertFalse(todo.isDone()); // is set as false by default
        Assertions.assertEquals(Priority.LOW, todo.getPriority());
    }

    @Test
    public void testSaveTodoWithInvalidDate() {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .deadline(today.minusDays(5))
                .build();

        Exception pastDateException = Assertions.assertThrows(DeadlineCannotBeInPastException.class,
                () -> todoService.saveTodo(todoSaveDto));
        Assertions.assertEquals("Deadline cannot be in past", pastDateException.getMessage());
    }


    @Test
    public void testGetFrontendTodoDtoList() {
        TodoSaveDto todoSaveDto1 = TodoSaveDto.builder()
                .name("Todo 1")
                .isDone(false)
                .build();

        todoService.saveTodo(todoSaveDto1);

        TodoSaveDto todoSaveDto2 = TodoSaveDto.builder()
                .name("Todo 2")
                .isDone(true)
                .deadline(today.plusDays(9))
                .priority(Priority.HIGH)
                .build();

        todoService.saveTodo(todoSaveDto2);

        List<TodoFrontendDto> frontendDtoList = todoService.getFrontedTodoDtoList();


        Assertions.assertEquals(2, frontendDtoList.size());

        TodoFrontendDto dto1 = frontendDtoList.get(0);
        Assertions.assertNotNull(dto1.getTodoId(), "todo id should not be null");
        Assertions.assertEquals("Todo 1", dto1.getName());
        Assertions.assertEquals("No deadline", dto1.getDeadline());
        Assertions.assertEquals("No priority", dto1.getPriority());
        Assertions.assertEquals("Not finished", dto1.getIsDone());

        TodoFrontendDto dto2 = frontendDtoList.get(1);
        Assertions.assertNotNull(dto2.getTodoId(), "todo id should not be null");
        Assertions.assertEquals("Todo 2", dto2.getName());
        Assertions.assertEquals(today.plusDays(9).toString(), dto2.getDeadline());
        Assertions.assertEquals("HIGH", dto2.getPriority());
        Assertions.assertEquals("Finished", dto2.getIsDone());
    }

    @Test
    public void testUpdateTodoNameWithValidId() {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .build();
        Todo todo = todoService.saveTodo(todoSaveDto);

        Todo updatedTodo = todoService.updateTodoName(todo.getTodoId(), "New todo");
        Assertions.assertEquals("New todo", updatedTodo.getName());
    }

    @Test
    public void testUpdateTodoNameWithInvalidId() {
        long nonExistentTodoId = 99L;
        Exception exception = Assertions.assertThrows(Exception.class,
                () -> todoService.updateTodoName(nonExistentTodoId, "Different name"));
        Assertions.assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void testChangeTodoIsDoneStatusWhenInitiallyFalse() {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .isDone(false)
                .build();
        Todo todo = todoService.saveTodo(todoSaveDto);

        Todo updatedTodo = todoService.changeTodoIsDoneStatus(todo.getTodoId());
        Assertions.assertTrue(updatedTodo.isDone());
    }

    @Test
    public void testChangeTodoIsDoneStatusWhenInitiallyTrue() {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .isDone(true)
                .build();
        Todo todo = todoService.saveTodo(todoSaveDto);

        Todo updatedTodo = todoService.changeTodoIsDoneStatus(todo.getTodoId());
        Assertions.assertFalse(updatedTodo.isDone());
    }

    @Test
    public void testChangeTodoIsDoneStatusWithInvalidId() {
        long nonExistentTodoId = 99L;
        Exception exception = Assertions.assertThrows(Exception.class,
                () -> todoService.changeTodoIsDoneStatus(nonExistentTodoId));
        Assertions.assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void testUpdateTodoDeadlineWithValidId() {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .deadline(today.plusDays(1))
                .build();
        Todo todo = todoService.saveTodo(todoSaveDto);

        Todo updatedTodo = todoService.updateTodoDeadline(todo.getTodoId(), today.plusDays(10));
        Assertions.assertEquals(today.plusDays(10), updatedTodo.getDeadline());
        Assertions.assertNotEquals(todo.getDeadline(), updatedTodo.getDeadline());
    }

    @Test
    public void testUpdateTodoDeadlineWithInvalidId() {
        long nonExistentTodoId = 99L;
        Exception exception = Assertions.assertThrows(Exception.class,
                () -> todoService.updateTodoDeadline(nonExistentTodoId, today));
        Assertions.assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void testUpdateTodoDeadlineWithInvalidDate() {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .deadline(today.plusDays(1))
                .build();
        Todo todo = todoService.saveTodo(todoSaveDto);

        Exception pastDateException = Assertions.assertThrows(DeadlineCannotBeInPastException.class,
                () -> todoService.updateTodoDeadline(todo.getTodoId(), today.minusDays(10)));
        Assertions.assertEquals("Deadline cannot be in past", pastDateException.getMessage());
    }

    @Test
    public void testUpdateTodoPriorityWithValidId() {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .priority(Priority.LOW)
                .build();
        Todo todo = todoService.saveTodo(todoSaveDto);

        Todo updatedTodo = todoService.updateTodoPriority(todo.getTodoId(), Priority.MEDIUM);
        Assertions.assertEquals(Priority.MEDIUM, updatedTodo.getPriority());
        Assertions.assertNotEquals(todo.getPriority(), updatedTodo.getPriority());
    }

    @Test
    public void testUpdateTodoPriorityWithInvalidId() {
        long nonExistentTodoId = 99L;
        Exception exception = Assertions.assertThrows(Exception.class,
                () -> todoService.updateTodoPriority(nonExistentTodoId, Priority.HIGH));
        Assertions.assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void testDeleteTodoWithValidId() {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .build();
        Todo todo = todoService.saveTodo(todoSaveDto);

        todoService.deleteTodo(todo.getTodoId());
        Optional<Todo> deletedTodo = todoRepository.findById(todo.getTodoId());
        Assertions.assertFalse(deletedTodo.isPresent());
    }

    @Test
    public void testDeleteTodoWithInvalidId() {
        long nonExistentTodoId = 99;
        Exception exception = Assertions.assertThrows(Exception.class,
                () -> todoService.deleteTodo(nonExistentTodoId));
        Assertions.assertEquals("Todo not found", exception.getMessage());
    }

}
