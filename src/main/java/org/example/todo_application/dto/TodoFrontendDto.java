package org.example.todo_application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TodoFrontendDto {
    private Long todoId;
    private String name;
    private String isDone;
    private String deadline;
    private String priority;
}
