package org.example.todo_application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.todo_application.dto.TodoFrontendDto;
import org.example.todo_application.dto.TodoSaveDto;
import org.example.todo_application.entity.Priority;
import org.example.todo_application.exception.DeadlineCannotBeInPastException;
import org.example.todo_application.exception.GlobalExceptionHandler;
import org.example.todo_application.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RestControllerUnitTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TodoService todoService;

    @InjectMocks
    private RestController restController;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(restController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Current date is stored in a variable to ensure consistent date comparisons
    // throughout the tests, avoiding potential timing issues if the test runs around
    // midnight and `LocalDate.now()` is called multiple times.
    LocalDate today = LocalDate.now();


    @Test
    public void testCreateValidTodo() throws Exception {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .priority(Priority.LOW)
                .build();

        mockMvc.perform(post("/api/restController/createTodo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoSaveDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Todo: Example todo was saved"));

        verify(todoService, times(1)).saveTodo(any(TodoSaveDto.class));
    }

    @Test
    public void testCreateInvalidTodoWithBlankAndNullName() throws Exception {
        // blank name
        TodoSaveDto todoSaveDto1 = TodoSaveDto.builder()
                .name("")
                .build();

        mockMvc.perform(post("/api/restController/createTodo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoSaveDto1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("must not be blank"));

        // null name
        TodoSaveDto todoSaveDto2 = TodoSaveDto.builder()
                .priority(Priority.LOW)
                .build();

        mockMvc.perform(post("/api/restController/createTodo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoSaveDto2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("must not be blank"));

        verify(todoService, times(0)).saveTodo(any(TodoSaveDto.class));
    }

    @Test
    public void testGetFrontendTodoList() throws Exception {
        List<TodoFrontendDto> todoList = new ArrayList<>();
        todoList.add(new TodoFrontendDto(1L, "Todo 1", "false", null, "LOW"));
        todoList.add(new TodoFrontendDto(2L, "Todo 2", "true", null, "HIGH"));

        when(todoService.getFrontedTodoDtoList()).thenReturn(todoList);

        mockMvc.perform(get("/api/restController/todos/frontendDto"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(todoList)));

        verify(todoService, times(1)).getFrontedTodoDtoList();
    }

    @Test
    public void testUpdateTodoNameWithValidId() throws Exception {
        mockMvc.perform(patch("/api/restController/updateName")
                        .param("todoId", "1")
                        .param("newName", "Updated Todo"))
                .andExpect(status().isOk())
                .andExpect(content().string("Todo renamed to: Updated Todo"));

        verify(todoService, times(1)).updateTodoName(1L, "Updated Todo");
    }

    @Test
    public void testUpdateTodoNameWithInvalidId() throws Exception {
        long nonExistentTodoId = 99L;

        doThrow(new RuntimeException("Todo not found"))
                .when(todoService).updateTodoName(nonExistentTodoId, "Updated Todo");

        mockMvc.perform(patch("/api/restController/updateName")
                        .param("todoId",String.valueOf(nonExistentTodoId))
                        .param("newName", "Updated Todo"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Todo not found"));

        verify(todoService, times(1)).updateTodoName(nonExistentTodoId, "Updated Todo");
    }

    @Test
    void testChangeTodoIsDoneStatusValidTodo() throws Exception {
        mockMvc.perform(patch("/api/restController/changeIsDoneStatus")
                        .param("todoId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Status was changed"));

        verify(todoService, times(1)).changeTodoIsDoneStatus(1L);
    }

    @Test
    void testChangeTodoIsDoneStatusWithInvalidId() throws Exception {
        long nonExistentTodoId = 99L;

        doThrow(new RuntimeException("Todo not found"))
                .when(todoService).changeTodoIsDoneStatus(nonExistentTodoId);

        mockMvc.perform(patch("/api/restController/changeIsDoneStatus")
                        .param("todoId", String.valueOf(nonExistentTodoId)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Todo not found"));

        verify(todoService, times(1)).changeTodoIsDoneStatus(nonExistentTodoId);
    }

    @Test
    void testUpdateTodoDeadlineWithValidId() throws Exception {
        mockMvc.perform(patch("/api/restController/updateDeadline")
                        .param("todoId", "1")
                        .param("newLocalDate", today.plusDays(10).toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(String.format("Deadline adjusted to: %s", today.plusDays(10))));

        verify(todoService, times(1)).updateTodoDeadline(eq(1L), any(LocalDate.class));
    }

    @Test
    void testUpdateTodoDeadlineWithInvalidId() throws Exception {
        long nonExistentTodoId = 99L;

        doThrow(new RuntimeException("Todo not found"))
                .when(todoService).updateTodoDeadline(nonExistentTodoId,today.plusDays(10));

        mockMvc.perform(patch("/api/restController/updateDeadline")
                        .param("todoId", String.valueOf(nonExistentTodoId))
                        .param("newLocalDate", today.plusDays(10).toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Todo not found"));

        verify(todoService, times(1)).updateTodoDeadline(nonExistentTodoId, today.plusDays(10));
    }

    @Test
    void testUpdateTodoDeadlineWithInvalidDate() throws Exception {

        doThrow(new DeadlineCannotBeInPastException("Deadline cannot be in past"))
                .when(todoService).updateTodoDeadline(1L,today.minusDays(10));

        mockMvc.perform(patch("/api/restController/updateDeadline")
                        .param("todoId", "1")
                        .param("newLocalDate", today.minusDays(10).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Deadline cannot be in past"));

        verify(todoService, times(1)).updateTodoDeadline(eq(1L), eq(today.minusDays(10)));
    }

    @Test
    void testUpdateTodoPriority() throws Exception {
        mockMvc.perform(patch("/api/restController/updatePriority")
                        .param("todoId", "1")
                        .param("priority", "MEDIUM"))
                .andExpect(status().isOk())
                .andExpect(content().string("Priority adjusted to: MEDIUM"));

        verify(todoService, times(1)).updateTodoPriority(1L, Priority.MEDIUM);
    }

    @Test
    void testUpdateTodoPriorityWithInvalidId() throws Exception {
        long nonExistentTodoId = 99L;

        doThrow(new RuntimeException("Todo not found"))
                .when(todoService).updateTodoPriority(nonExistentTodoId,Priority.LOW);

        mockMvc.perform(patch("/api/restController/updatePriority")
                        .param("todoId",String.valueOf(nonExistentTodoId))
                        .param("priority","LOW"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Todo not found"));

        verify(todoService, times(1)).updateTodoPriority(nonExistentTodoId, Priority.LOW);
    }

    @Test
    void testDeleteTodoWithValidId() throws Exception {
        mockMvc.perform(delete("/api/restController/deleteTodo")
                        .param("todoId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Todo was deleted"));

        verify(todoService, times(1)).deleteTodo(1L);
    }

    @Test
    void testDeleteTodoWithInvalidId() throws Exception {
        long nonExistentTodoId = 99L;

        doThrow(new RuntimeException("Todo not found"))
                .when(todoService).deleteTodo(nonExistentTodoId);

        mockMvc.perform(delete("/api/restController/deleteTodo")
                        .param("todoId", String.valueOf(nonExistentTodoId)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Todo not found"));

        verify(todoService, times(1)).deleteTodo(nonExistentTodoId);
    }
}

