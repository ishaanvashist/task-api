package com.ishu.task_api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;          // fake repository, controlled by us, no real storage

    @InjectMocks
    private TaskService taskService;                // real TaskService, but fake repository plugged into its constructor

    @Test
    void createTask_shouldReturnSavedTask() {
        // Arrange
        Task inputTask = new Task();
        inputTask.setTitle("Buy groceries");
        inputTask.setDescription("Milk, eggs, bread");
        inputTask.setCompleted(false);

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle("Buy groceries");
        savedTask.setDescription("Milk, eggs, bread");
        savedTask.setCompleted(false);

        when(taskRepository.save(inputTask)).thenReturn(savedTask);

        // Act
        Task result = taskService.createTask(inputTask);

        // Assert
        assertEquals(1L, result.getId());
        assertEquals("Buy groceries", result.getTitle());
    }

    @Test
    void getTaskById_shouldThrowException_whenTaskNotFound() {
        // Arrange
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(TaskNotFoundException.class, () -> {
            taskService.getTaskById(99L);
        });
    }

    @Test
    void updateTask_shouldReturnUpdatedTask_whenTaskExists() {
        // Arrange
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Old title");

        Task updatedTask = new Task();
        updatedTask.setTitle("New title");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));   // ← THIS is the one line that changed
        when(taskRepository.save(updatedTask)).thenReturn(updatedTask);

        // Act
        Task result = taskService.updateTask(1L, updatedTask);

        // Assert
        assertEquals("New title", result.getTitle());
        assertEquals(1L, result.getId());
    }

}