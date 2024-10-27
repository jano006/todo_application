package org.example.todo_application.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.todo_application.dto.TodoSaveDto;
import org.example.todo_application.entity.Priority;
import org.example.todo_application.service.TodoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MvcController {
    private final TodoService todoService;

    @PostMapping("/createTodo")
    public String createTodo(@Valid TodoSaveDto todoSaveDto,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("todoSaveDto", todoSaveDto);
            return "index";
        }
        try {
            todoService.saveTodo(todoSaveDto);
            return "redirect:/";
        } catch (Exception e) {
            log.error("Failed to create todo: {} ", e.getMessage());
            model.addAttribute("errormessage", "Failed to create todo: " + e.getMessage());
            return "error-page";
        }
    }

    @GetMapping()
    public String getFrontendTodoList(Model model) {
        try {
            model.addAttribute("todos", todoService.getFrontedTodoDtoList());
            model.addAttribute("todoSaveDto", new TodoSaveDto());
            return "index";
        } catch (Exception e) {
            log.error("Failed to get list of todos: {} ", e.getMessage());
            model.addAttribute("errormessage", "Failed to get todos: " + e.getMessage());
            return "error-page";
        }
    }

    @PostMapping("/updateName")
    public String updateName(@RequestParam Long todoId,
                             @RequestParam String newName,
                             Model model) {
        if (newName.length() > 100) {
            model.addAttribute("errormessage", "Failed to update todo: Name cannot exceed 100 characters.");
            return "error-page";
        }
        try {
            todoService.updateTodoName(todoId, newName);
            return "redirect:/";

        } catch (Exception e) {
            log.error("Failed to update todo with id {}: {}", todoId, e.getMessage());
            model.addAttribute("errormessage", "Failed to update todo: " + e.getMessage());
            return "error-page";
        }
    }

    @PostMapping("/updateIsDoneStatus")
    public String changeIsDoneStatus(@RequestParam Long todoId,
                                     Model model) {
        try {
            todoService.changeTodoIsDoneStatus(todoId);
            return "redirect:/";

        } catch (Exception e) {
            log.error("Failed to change status for todo with id {}: {}", todoId, e.getMessage());
            model.addAttribute("errormessage", "Failed to change todo status: " + e.getMessage());
            return "error-page";
        }
    }

    @PostMapping("/updateDeadline")
    public String updateDeadline(@RequestParam Long todoId,
                                 @RequestParam(required = false) String newLocalDate, // String for flexible input to handle null and date inputs
                                 Model model) {
        try {
            LocalDate deadline = (newLocalDate == null || newLocalDate.isEmpty()) ? null : LocalDate.parse(newLocalDate);
            todoService.updateTodoDeadline(todoId, deadline);
            return "redirect:/";
        } catch (Exception e) {
            log.error("Failed to update deadline for todo with id {}: {}", todoId, e.getMessage());
            model.addAttribute("errormessage", "Failed to update todo deadline: " + e.getMessage());
            return "error-page";
        }
    }


    @PostMapping("/updatePriority")
    public String updatePriority(@RequestParam Long todoId,
                                 @RequestParam(required = false) String priority, // String for flexible input
                                 Model model) {
        try {
            Priority priorityValue = (priority == null || priority.isEmpty() || "null".equals(priority)) ? null : Priority.valueOf(priority);

            todoService.updateTodoPriority(todoId, priorityValue);
            return "redirect:/";

        } catch (Exception e) {
            log.error("Failed to update priority for todo with id {}: {}", todoId, e.getMessage());
            model.addAttribute("errormessage", "Failed to update priority: " + e.getMessage());
            return "error-page";
        }
    }


    @PostMapping("/deleteTodo")
    public String deleteTodo(@RequestParam Long todoId, Model model) {
        try {
            todoService.deleteTodo(todoId);
            return "redirect:/";
        } catch (Exception e) {
            log.error("Failed to delete todo with id {}: {}", todoId, e.getMessage());
            model.addAttribute("errormessage", "Failed to delete todo: " + e.getMessage());
            return "error-page";
        }
    }
}
