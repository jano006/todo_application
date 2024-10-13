package org.example.todo_application.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long todoId;
    @Column(length = 100)
    private String name;
    private boolean isDone;
    private LocalDate deadline;
    @Enumerated(EnumType.STRING)
    private Priority priority;

    public void changeIsDoneStatus() {
        this.isDone = !isDone;
    }

}
