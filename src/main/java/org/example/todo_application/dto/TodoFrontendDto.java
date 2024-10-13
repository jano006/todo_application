package org.example.todo_application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TodoFrontendDto {
    private Long todoId;
    private String name;
    private String isDone;
    private String deadline;
    private String priority;
}
