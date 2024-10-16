package org.example.todo_application.service;

import lombok.RequiredArgsConstructor;
import org.example.todo_application.dto.TodoFrontendDto;
import org.example.todo_application.dto.TodoSaveDto;
import org.example.todo_application.entity.Priority;
import org.example.todo_application.entity.Todo;
import org.example.todo_application.exception.DeadlineCannotBeInPastException;
import org.example.todo_application.mapper.TodoMapper;
import org.example.todo_application.repository.TodoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TodoService {
    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;

    public Todo saveTodo(TodoSaveDto todoSaveDto) {
        if (todoSaveDto.getDeadline() != null && todoSaveDto.getDeadline().isBefore(LocalDate.now())) {
            throw new DeadlineCannotBeInPastException("Deadline cannot be in past");
        }
        Todo todo = Todo.builder()
                .name(todoSaveDto.getName())
                .isDone(todoSaveDto.isDone())
                .deadline(todoSaveDto.getDeadline())
                .priority(todoSaveDto.getPriority())
                .build();
        return todoRepository.save(todo);
    }

    public List<TodoFrontendDto> getFrontedTodoDtoList() {
        List<Todo> todoList = todoRepository.findAll();
        return todoList.stream()
                .map(todoMapper::entityToFrontEndDto)
                .collect(Collectors.toList());
    }

    public Todo updateTodoName(Long todoId, String newName) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        todo.setName(newName);
        return todoRepository.save(todo);
    }

    public Todo changeTodoIsDoneStatus(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        todo.changeIsDoneStatus();
        return todoRepository.save(todo);
    }

    public Todo updateTodoDeadline(Long todoId, LocalDate newLocalDate) {
        if (newLocalDate != null && newLocalDate.isBefore(LocalDate.now())) {
            throw new DeadlineCannotBeInPastException("Deadline cannot be in past");
        }
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        todo.setDeadline(newLocalDate);
        return todoRepository.save(todo);
    }

    public Todo updateTodoPriority(Long todoId, Priority newPriority) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        todo.setPriority(newPriority);
        return todoRepository.save(todo);
    }

    public void deleteTodo(Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found"));
        todoRepository.delete(todo);

    }
}
