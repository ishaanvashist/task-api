package com.ishu.task_api;

import org.springframework.data.jpa.repository.JpaRepository;                        // gives you the base interface with CRUD methods
import org.springframework.stereotype.Repository;                                    // marks it as a Spring bean

@Repository                                                                          // Spring registers this as a repository bean
public interface TaskRepository extends JpaRepository<Task, Long> {                  // extends JpaRepository → all CRUD methods auto-provided
}