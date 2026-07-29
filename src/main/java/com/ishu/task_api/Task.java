package com.ishu.task_api;

import jakarta.persistence.Entity;                                                   // marks the class as a database entity
import jakarta.persistence.GeneratedValue;                                           // for auto-generating id values
import jakarta.persistence.GenerationType;                                           // for choosing the id generation strategy
import jakarta.persistence.Id;                                                        // marks the primary key
import jakarta.persistence.Table;                                                     // lets us specify the table name
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity                                                                              // "hey Hibernate, this is a database entity"
@Table(name = "tasks")                                                                // maps to a table named 'tasks' (plural, standard convention)
public class Task {

    @Id                                                                              // this field is the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)                              // let the database auto-generate id values
    private Long id;

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private boolean completed;

    public Task() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}