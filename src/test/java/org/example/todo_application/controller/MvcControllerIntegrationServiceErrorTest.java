package org.example.todo_application.controller;

import org.example.todo_application.dto.TodoSaveDto;
import org.example.todo_application.service.TodoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MvcControllerIntegrationServiceErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TodoService todoService;

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

}
