package org.example.todo_application.controller;

import org.example.todo_application.dto.TodoSaveDto;
import org.example.todo_application.entity.Priority;
import org.example.todo_application.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MvcControllerUnitTest {

    private MockMvc mockMvc;

    @Mock
    private TodoService todoService;

    @InjectMocks
    private MvcController mvcController;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(mvcController).build();
    }

    // Current date is stored in a variable to ensure consistent date comparisons
    // throughout the tests, avoiding potential timing issues if the test runs around
    // midnight and `LocalDate.now()` is called multiple times.
    LocalDate today = LocalDate.now();

    @Test
    public void testCreateValidTodo() throws Exception {
        mockMvc.perform(post("/createTodo")
                        .param("name", "Example todo")
                        .param("isDone", "false")
                        .param("deadline", "")
                        .param("priority", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(todoService, times(1)).saveTodo(any(TodoSaveDto.class));

    }

    @Test
    public void testCreateInvalidTodoWithBlankAndNullName() throws Exception {
        // blank
        mockMvc.perform(post("/createTodo")
                        .param("name", "")
                        .param("isDone", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeHasFieldErrors("todoSaveDto", "name"));

        // null
        mockMvc.perform(post("/createTodo")
                        .param("isDone", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeHasFieldErrors("todoSaveDto", "name"));
    }

    @Test
    public void testCreateTodoWithServiceError() throws Exception {
        doThrow(new RuntimeException("Service failed"))
                .when(todoService).saveTodo(any(TodoSaveDto.class));

        mockMvc.perform(post("/createTodo")
                        .param("name", "Valid Todo Name")
                        .param("isDone", "false")
                        .param("deadline", "")
                        .param("priority", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("errormessage"))
                .andExpect(model().attribute("errormessage", "Failed to create todo: Service failed"));
    }

    @Test
    public void testGetFrontendTodoList() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("todos"))
                .andExpect(model().attributeExists("todoSaveDto"));

        verify(todoService, times(1)).getFrontedTodoDtoList();
    }

    @Test
    public void testUpdateTodoNameWithValidId() throws Exception {
        mockMvc.perform(post("/updateName")
                        .param("todoId", "1")
                        .param("newName", "New todo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(todoService, times(1)).updateTodoName(1L, "New todo");
    }

    @Test
    public void testUpdateTodoNameWithInvalidId() throws Exception {
        doThrow(new RuntimeException("Todo not found"))
                .when(todoService).updateTodoName(99L, "New todo");

        mockMvc.perform(post("/updateName")
                        .param("todoId", "99")
                        .param("newName", "New todo"))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("errormessage"))
                .andExpect(model().attribute("errormessage", "Failed to update todo: Todo not found"));
    }

    @Test
    public void testChangeTodoIsDoneStatusWithValidId() throws Exception {
        mockMvc.perform(post("/updateIsDoneStatus")
                        .param("todoId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(todoService, times(1)).changeTodoIsDoneStatus(1L);
    }

    @Test
    public void testChangeTodoIsDoneStatusWithInvalidId() throws Exception {
        doThrow(new RuntimeException("Todo not found"))
                .when(todoService).changeTodoIsDoneStatus(99L);

        mockMvc.perform(post("/updateIsDoneStatus")
                        .param("todoId", "99"))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("errormessage"))
                .andExpect(model().attribute("errormessage", "Failed to change todo status: Todo not found"));
    }

    @Test
    public void testUpdateTodoDeadlineWithValidId() throws Exception {
        mockMvc.perform(post("/updateDeadline")
                        .param("todoId", "1")
                        .param("newLocalDate", today.plusDays(10).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(todoService, times(1)).updateTodoDeadline(eq(1L), eq(today.plusDays(10)));
    }

    @Test
    public void testUpdateTodoDeadlineWithInvalidId() throws Exception {
        doThrow(new RuntimeException("Todo not found"))
                .when(todoService).updateTodoDeadline(eq(99L), any(LocalDate.class));

        mockMvc.perform(post("/updateDeadline")
                        .param("todoId", "99")
                        .param("newLocalDate", today.plusDays(10).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("errormessage"))
                .andExpect(model().attribute("errormessage", "Failed to update todo deadline: Todo not found"));
    }

    @Test
    public void testUpdateTodoPriorityWithValidId() throws Exception {
        mockMvc.perform(post("/updatePriority")
                        .param("todoId", "1")
                        .param("priority", Priority.MEDIUM.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(todoService, times(1)).updateTodoPriority(1L, Priority.MEDIUM);
    }

    @Test
    public void testUpdateTodoPriorityWithInvalidId() throws Exception {
        doThrow(new RuntimeException("Todo not found"))
                .when(todoService).updateTodoPriority(99L, Priority.MEDIUM);

        mockMvc.perform(post("/updatePriority")
                        .param("todoId", "99")
                        .param("priority", Priority.MEDIUM.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("errormessage"))
                .andExpect(model().attribute("errormessage", "Failed to update priority: Todo not found"));
    }

    @Test
    public void testDeleteTodoWithValidId() throws Exception {
        mockMvc.perform(post("/deleteTodo")
                        .param("todoId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(todoService, times(1)).deleteTodo(1L);
    }

    @Test
    public void testDeleteTodoWithInvalidId() throws Exception {
        doThrow(new RuntimeException("Todo not found"))
                .when(todoService).deleteTodo(99L);

        mockMvc.perform(post("/deleteTodo")
                        .param("todoId", "99"))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("errormessage"))
                .andExpect(model().attribute("errormessage", "Failed to delete todo: Todo not found"));
    }
}
