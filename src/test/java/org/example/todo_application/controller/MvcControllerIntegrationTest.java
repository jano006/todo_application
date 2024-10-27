package org.example.todo_application.controller;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MvcControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoService todoService;

    @Autowired
    private TodoRepository todoRepository;

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
        mockMvc.perform(post("/createTodo")
                        .param("name", "Example todo")
                        .param("isDone", "false")
                        .param("deadline", "")
                        .param("priority", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Todo createdTodo = todoRepository.findAll().get(0);
        Assertions.assertEquals("Example todo",createdTodo.getName());
    }

    @Test
    public void testCreateInvalidTodoWithBlankAndNullName() throws Exception {
        mockMvc.perform(post("/createTodo")
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeHasFieldErrors("todoSaveDto", "name"));

        mockMvc.perform(post("/createTodo")
                        .param("isDone", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeHasFieldErrors("todoSaveDto", "name"));

        Assertions.assertEquals(0,todoRepository.count());
    }


    @Test
    public void testGetFrontendTodoList() throws Exception {
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

        MvcResult mvcResult = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("todos"))
                .andExpect(model().attributeExists("todoSaveDto"))
                .andReturn();

        List<?> todos = (List<?>) mvcResult.getModelAndView().getModel().get("todos");
        Assertions.assertNotNull(todos, "Todos list should not be null");
        Assertions.assertEquals(2, todos.size(), "There should be 2 todos in the list");

        TodoFrontendDto todo1 = (TodoFrontendDto) todos.get(0);
        TodoFrontendDto todo2 = (TodoFrontendDto) todos.get(1);

        Assertions.assertEquals("Todo 1", todo1.getName());
        Assertions.assertEquals("Todo 2", todo2.getName());
        Assertions.assertEquals("Not finished", todo1.getIsDone());
        Assertions.assertEquals("Finished", todo2.getIsDone());
        Assertions.assertEquals("No priority", todo1.getPriority());
        Assertions.assertEquals("HIGH", todo2.getPriority());
    }


    @Test
    public void testUpdateTodoNameWithValidId() throws Exception {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example Todo")
                .build();
        Todo savedTodo = todoService.saveTodo(todoSaveDto);

        mockMvc.perform(post("/updateName")
                        .param("todoId", savedTodo.getTodoId().toString())
                        .param("newName", "New todo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Todo updatedTodo = todoRepository.findById(savedTodo.getTodoId()).orElseThrow();
        Assertions.assertEquals("New todo",updatedTodo.getName());
    }

    @Test
    public void testUpdateTodoNameWithInvalidId() throws Exception {
        long nonExistentTodoId = 99L;

        mockMvc.perform(post("/updateName")
                        .param("todoId", String.valueOf(nonExistentTodoId))
                        .param("newName", "New todo"))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("errormessage"))
                .andExpect(model().attribute("errormessage", "Failed to update todo: Todo not found"));
    }

    @Test
    public void testChangeTodoIsDoneStatusWithValidId() throws Exception {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example Todo")
                .isDone(false)
                .build();
        Todo savedTodo = todoService.saveTodo(todoSaveDto);

        mockMvc.perform(post("/updateIsDoneStatus")
                        .param("todoId", savedTodo.getTodoId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Todo updatedTodo = todoRepository.findById(savedTodo.getTodoId()).orElseThrow();
        Assertions.assertTrue(updatedTodo.isDone());
    }

    @Test
    public void testChangeTodoIsDoneStatusWithInvalidId() throws Exception {
        long nonExistentTodoId = 99L;

        mockMvc.perform(post("/updateIsDoneStatus")
                        .param("todoId", String.valueOf(nonExistentTodoId)))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("errormessage"))
                .andExpect(model().attribute("errormessage", "Failed to change todo status: Todo not found"));
    }

    @Test
    public void testUpdateTodoDeadlineWithValidId() throws Exception {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .build();
        Todo savedTodo = todoService.saveTodo(todoSaveDto);

        mockMvc.perform(post("/updateDeadline")
                        .param("todoId", savedTodo.getTodoId().toString())
                        .param("newLocalDate", today.plusDays(10).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Todo updatedTodo = todoRepository.findById(savedTodo.getTodoId()).orElseThrow();
        Assertions.assertEquals(today.plusDays(10),updatedTodo.getDeadline());
    }

    @Test
    public void testUpdateTodoDeadlineWithInvalidId() throws Exception {
        long nonExistentTodoId = 99L;

        mockMvc.perform(post("/updateDeadline")
                        .param("todoId", String.valueOf(nonExistentTodoId))
                        .param("newLocalDate", today.plusDays(10).toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("errormessage"))
                .andExpect(model().attribute("errormessage", "Failed to update todo deadline: Todo not found"));
    }

    @Test
    public void testUpdateTodoPriorityWithValidId() throws Exception {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .priority(Priority.LOW)
                .build();
        Todo savedTodo = todoService.saveTodo(todoSaveDto);

        mockMvc.perform(post("/updatePriority")
                        .param("todoId", savedTodo.getTodoId().toString())
                        .param("priority",Priority.MEDIUM.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Todo updatedTodo = todoRepository.findById(savedTodo.getTodoId()).orElseThrow();
        Assertions.assertEquals(Priority.MEDIUM,updatedTodo.getPriority());
    }

    @Test
    public void testUpdateTodoPriorityWithInvalidId() throws Exception {
        long nonExistentTodoId = 99L;

        mockMvc.perform(post("/updatePriority")
                        .param("todoId", String.valueOf(nonExistentTodoId))
                        .param("priority",Priority.MEDIUM.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("errormessage"))
                .andExpect(model().attribute("errormessage", "Failed to update priority: Todo not found"));
    }

    @Test
    public void testDeleteTodoWithValidId() throws Exception {
        TodoSaveDto todoSaveDto = TodoSaveDto.builder()
                .name("Example todo")
                .build();
        Todo savedTodo = todoService.saveTodo(todoSaveDto);

        mockMvc.perform(post("/deleteTodo")
                        .param("todoId", savedTodo.getTodoId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        Assertions.assertEquals(0,todoRepository.count());
    }

    @Test
    public void testDeleteTodoWithInvalidId() throws Exception {
        long nonExistentTodoId = 99L;

        mockMvc.perform(post("/deleteTodo")
                        .param("todoId", String.valueOf(nonExistentTodoId)))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andExpect(model().attributeExists("errormessage"))
                .andExpect(model().attribute("errormessage", "Failed to delete todo: Todo not found"));
    }
}
