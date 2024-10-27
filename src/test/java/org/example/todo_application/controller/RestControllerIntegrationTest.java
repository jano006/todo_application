package org.example.todo_application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.todo_application.dto.TodoFrontendDto;
import org.example.todo_application.dto.TodoSaveDto;
import org.example.todo_application.entity.Priority;
import org.example.todo_application.entity.Todo;
import org.example.todo_application.repository.TodoRepository;
import org.example.todo_application.service.TodoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class RestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoService todoService;

    @BeforeEach
    public void setup() {
        todoRepository.deleteAll();
    }

    // Current date is stored in a variable to ensure consistent date comparisons
    // throughout the tests, avoiding potential timing issues if the test runs around
    // midnight and `LocalDate.now()` is called multiple times.
    LocalDate today = LocalDate.now();

    @Test
    public void testCreateValidTodo() throws Exception {
        // Arrange
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .priority(Priority.MEDIUM)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/restController/createTodo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoSaveDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string(String.format("Todo: %s was saved", todoSaveDto.getName())));

        Assertions.assertEquals(1, todoRepository.count());
        Assertions.assertTrue(todoRepository.findAll().stream()
                .anyMatch(todo -> todo.getName().equals("Example todo") && todo.getPriority() == Priority.MEDIUM));
    }

    @Test
    public void testCreateInvalidTodoWithBlankAndNullName() throws Exception {
        // blank
        // Arrange
        TodoSaveDto todoSaveDto1 = TodoSaveDto.builder()
                .name("")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/restController/createTodo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoSaveDto1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("must not be blank"));

        // null
        // Arrange
        TodoSaveDto todoSaveDto2 = TodoSaveDto.builder()
                .priority(Priority.MEDIUM)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/restController/createTodo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoSaveDto2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("must not be blank"));

        Assertions.assertEquals(0, todoRepository.count());
    }

    @Test
    public void testGetFrontendTodoList() throws Exception {
        // Arrange
        TodoSaveDto todoSaveDto1 = TodoSaveDto.builder()
                .name("Todo 1")
                .isDone(false)
                .build();

        TodoSaveDto todoSaveDto2 = TodoSaveDto.builder()
                .name("Todo 2")
                .isDone(true)
                .priority(Priority.HIGH)
                .build();

        todoService.saveTodo(todoSaveDto1);
        todoService.saveTodo(todoSaveDto2);

        // Act & Assert
        mockMvc.perform(get("/api/restController/todos/frontendDto"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(todoService.getFrontedTodoDtoList())));

        List<TodoFrontendDto> dtoList = todoService.getFrontedTodoDtoList();

        Assertions.assertEquals(2, todoRepository.count(), "There should be 2 todos saved in the repository");
        Assertions.assertEquals("Todo 1", dtoList.get(0).getName());
        Assertions.assertEquals("Todo 2", dtoList.get(1).getName());
    }

    @Test
    public void testUpdateTodoNameWithValidId() throws Exception {
        // Arrange
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .build();

        Todo todo = todoService.saveTodo(todoSaveDto);

        // Act & Assert
        mockMvc.perform(patch("/api/restController/updateName")
                        .param("todoId", todo.getTodoId().toString())
                        .param("newName", "New todo"))
                .andExpect(status().isOk())
                .andExpect(content().string("Todo renamed to: New todo"));

        Todo updatedTodo = todoRepository.findById(todo.getTodoId()).orElseThrow();
        Assertions.assertEquals("New todo", updatedTodo.getName());
    }

    @Test
    public void testUpdateTodoNameWithInvalidId() throws Exception {
        // Arrange
        long nonExistentTodoId = 99L;

        // Act and Assert
        mockMvc.perform(patch("/api/restController/updateName")
                        .param("todoId", String.valueOf(nonExistentTodoId))
                        .param("newName", "Updated Todo Name"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Todo not found"));

        Assertions.assertEquals(0, todoRepository.count());

    }

    @Test
    public void testChangeTodoIsDoneStatusWithValidId() throws Exception {
        // Arrange
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .isDone(false)
                .build();

        Todo todo = todoService.saveTodo(todoSaveDto);

        // Act and Assert
        mockMvc.perform(patch("/api/restController/changeIsDoneStatus")
                        .param("todoId", todo.getTodoId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Status was changed"));

        Todo updatedTodo = todoRepository.findById(todo.getTodoId()).orElseThrow();
        Assertions.assertTrue(updatedTodo.isDone());
    }

    @Test
    public void testChangeTodoIsDoneStatusWithInvalidId() throws Exception {
        // Arrange
        long nonExistentTodoId = 99L;

        // Act and Assert
        mockMvc.perform(patch("/api/restController/changeIsDoneStatus")
                        .param("todoId", String.valueOf(nonExistentTodoId)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Todo not found"));

    }

    @Test
    public void testUpdateTodoDeadlineWithValidId() throws Exception {
        // Arrange
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .deadline(today.plusDays(1))
                .build();

        Todo todo = todoService.saveTodo(todoSaveDto);

        LocalDate newLocalDate = today.plusDays(10);

        // Act and Assert
        mockMvc.perform(patch("/api/restController/updateDeadline")
                        .param("todoId", todo.getTodoId().toString())
                        .param("newLocalDate", newLocalDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.format("Deadline adjusted to: %s", newLocalDate)));

        Todo updatedTodo = todoRepository.findById(todo.getTodoId()).orElseThrow();
        Assertions.assertEquals(today.plusDays(10), updatedTodo.getDeadline());
    }

    @Test
    public void testUpdateTodoDeadlineWithInvalidId() throws Exception {
        // Arrange
        long nonExistentTodoId = 99L;
        LocalDate newLocalDate = today.plusDays(10);

        // Act and Assert
        mockMvc.perform(patch("/api/restController/updateDeadline")
                        .param("todoId", String.valueOf(nonExistentTodoId))
                        .param("newLocalDate", newLocalDate.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Todo not found"));
    }

    @Test
    public void testUpdateTodoDeadlineWithInvalidDate() throws Exception {
        // Arrange
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .deadline(today.plusDays(1))
                .build();

        Todo todo = todoService.saveTodo(todoSaveDto);

        LocalDate pastDate = today.minusDays(10);

        // Act and Assert
        mockMvc.perform(patch("/api/restController/updateDeadline")
                        .param("todoId", todo.getTodoId().toString())
                        .param("newLocalDate", pastDate.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Deadline cannot be in past"));

    }

    @Test
    public void testUpdateTodoPriorityWithValidId() throws Exception {
        // Arrange
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .priority(Priority.LOW)
                .build();
        Todo todo = todoService.saveTodo(todoSaveDto);

        // Act and Assert
        mockMvc.perform(patch("/api/restController/updatePriority")
                        .param("todoId", todo.getTodoId().toString())
                        .param("priority", Priority.MEDIUM.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.format("Priority adjusted to: %s", Priority.MEDIUM)));

        Todo updatedTodo = todoRepository.findById(todo.getTodoId()).orElseThrow();
        Assertions.assertEquals(Priority.MEDIUM, updatedTodo.getPriority());
    }

    @Test
    public void testUpdateTodoPriorityWithInvalidId() throws Exception {
        // Arrange
        long nonExistentTodoId = 99L;

        // Act and Assert
        mockMvc.perform(patch("/api/restController/updatePriority")
                        .param("todoId", String.valueOf(nonExistentTodoId))
                        .param("priority", Priority.MEDIUM.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Todo not found"));

    }

    @Test
    public void testDeleteTodoWithValidId() throws Exception {
        // Arrange
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .priority(Priority.LOW)
                .build();
        Todo todo = todoService.saveTodo(todoSaveDto);

        // Act and Assert
        mockMvc.perform(delete("/api/restController/deleteTodo")
                        .param("todoId", todo.getTodoId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Todo was deleted"));

        Assertions.assertEquals(0, todoRepository.count());
    }

    @Test
    public void testDeleteTodoWithInvalidId() throws Exception {
        // Arrange
        long nonExistentTodoId = 99L;

        // Act and Assert
        mockMvc.perform(delete("/api/restController/deleteTodo")
                        .param("todoId", String.valueOf(nonExistentTodoId)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Todo not found"));
    }
}