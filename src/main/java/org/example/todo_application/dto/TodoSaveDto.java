package org.example.todo_application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.todo_application.entity.Priority;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoSaveDto {
    @NotBlank
    @Size(max = 100)
    private String name;
    private boolean isDone;
    private LocalDate deadline;
    private Priority priority;
}
