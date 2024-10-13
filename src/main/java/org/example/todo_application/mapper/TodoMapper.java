package org.example.todo_application.mapper;

import org.example.todo_application.dto.TodoFrontendDto;
import org.example.todo_application.entity.Todo;
import org.springframework.stereotype.Component;

@Component
public class TodoMapper {
    public TodoFrontendDto entityToFrontEndDto(Todo todo) {
        TodoFrontendDto todoFrontendDto = new TodoFrontendDto();
        todoFrontendDto.setTodoId(todo.getTodoId());
        todoFrontendDto.setName(todo.getName());
        todoFrontendDto.setIsDone(todo.isDone() ? "Finished" : "Not finished");
        todoFrontendDto.setDeadline(todo.getDeadline() == null ? "No deadline" : todo.getDeadline().toString());
        todoFrontendDto.setPriority(todo.getPriority() == null ? "No priority" : todo.getPriority().toString());
        return todoFrontendDto;
    }
}
