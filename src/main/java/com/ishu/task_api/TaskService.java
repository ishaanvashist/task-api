package com.ishu.task_api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        logger.info("Fetching all tasks");
        return taskRepository.findAll();                                             // unchanged — findAll still returns a plain list
    }

    public List<Task> getTasksByTitle(String title) {
        logger.info("Fetching tasks with title: {}", title);
        return taskRepository.findByTitle(title);
    }

    public Task getTaskById(Long id) {
        logger.info("Fetching task with id: {}", id);
        return taskRepository.findById(id)                                           // findById now returns a "box" (Optional) instead of a task or null
                .orElseThrow(() -> {                                                 // "give me the task, or else throw this error if the box is empty"
                    logger.warn("Task not found with id: {}", id);                  // logs the warning if the task wasn't found
                    return new TaskNotFoundException(id);                            // the error that gets thrown → global handler turns it into a 404
                });
    }

    public Task createTask(Task task) {
        logger.info("Creating task with title: {}", task.getTitle());
        return taskRepository.save(task);                                            // unchanged — save still works the same way from your side
    }

    public Task updateTask(Long id, Task updatedTask) {
        logger.info("Updating task with id: {}", id);
        Task existing = taskRepository.findById(id)                                  // same "give me the task, or else throw" pattern
                .orElseThrow(() -> {
                    logger.warn("Attempted to update non-existent task with id: {}", id);
                    return new TaskNotFoundException(id);
                });
        updatedTask.setId(id);                                                        // stamps the id from the URL onto the incoming task before saving
        return taskRepository.save(updatedTask);                                     // save it — behaves as an update because the id is set
    }

    public void deleteTask(Long id) {
        logger.info("Deleting task with id: {}", id);
        if (!taskRepository.existsById(id)) {                                        // lightweight "does this task exist?" check — doesn't load the row
            logger.warn("Attempted to delete non-existent task with id: {}", id);
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);                                                // delete it
    }
}