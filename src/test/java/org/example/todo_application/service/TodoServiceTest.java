package org.example.todo_application.service;

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

@SpringBootTest
public class TodoServiceTest {

    @Autowired
    private TodoService todoService;
    @Autowired
    private TodoRepository todoRepository;

    @BeforeEach
    void setup() {
        todoRepository.deleteAll();
    }

    @Test
    public void testSaveTodo() {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .deadline(LocalDate.now().plusDays(1))
                .priority(Priority.LOW)
                .build();

        Todo todo = todoService.saveTodo(todoSaveDto);

        Assertions.assertNotNull(todo.getTodoId());
        Assertions.assertEquals("Example todo",todo.getName());
        Assertions.assertFalse(todo.isDone());
        Assertions.assertEquals(LocalDate.now().plusDays(1),todo.getDeadline());
        Assertions.assertEquals(Priority.LOW,todo.getPriority());

        TodoSaveDto todoSaveDtoWithDateInPast = TodoSaveDto.builder()
                .name("Example todo 2")
                .deadline(LocalDate.now().minusDays(5))
                .build();

        DeadlineCannotBeInPastException exception = Assertions.assertThrows(DeadlineCannotBeInPastException.class,
                () -> todoService.saveTodo(todoSaveDtoWithDateInPast));
        Assertions.assertEquals("Deadline cannot be in past",exception.getMessage());

    }

}
