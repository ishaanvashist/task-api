package com.ishu.task_api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public List<Category> getAllCategoriesWithTaskCount() {
        logger.info("Fetching all categories (N+1 demonstration)");
        List<Category> categories = categoryRepository.findAllWithTasks();
        for (Category category : categories) {                            // loop through each category
            int taskCount = category.getTasks().size();                   // this line asks a SEPARATE question, per category
            logger.info("Category '{}' has {} task(s)", category.getName(), taskCount);
        }

        return categories;
    }
}