package com.ishu.task_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers                                                       // tells JUnit "this test class uses Testcontainers"
public class TaskControllerIntegrationTest {

    @Container                                                        // marks this as a container to start before tests, stop after
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")                  // same Postgres version you're already using
                    .withDatabaseName("taskdb")
                    .withUsername("taskuser")
                    .withPassword("taskpass");

    @DynamicPropertySource                                            // lets us hand Spring the real address AFTER the container starts
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllTasks_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    void createTask_withValidData_shouldReturn201() throws Exception {
        String requestBody = """
                {
                    "title": "Buy groceries",
                    "description": "Milk, eggs, bread",
                    "completed": false
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void createTask_withBlankTitle_shouldReturn400() throws Exception {
        String requestBody = """
                {
                    "title": "",
                    "description": "Missing title",
                    "completed": false
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTaskById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/tasks/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_afterCreating_shouldReturn204() throws Exception {
        String requestBody = """
                {
                    "title": "Task to delete",
                    "description": "Will be deleted",
                    "completed": false
                }
                """;

        String response = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Number createdId = com.jayway.jsonpath.JsonPath.read(response, "$.id");

        mockMvc.perform(delete("/api/tasks/" + createdId))
                .andExpect(status().isNoContent());
    }

    @Test
    void createTask_withOversizedTitle_shouldReturn400() throws Exception {
        String longTitle = "a".repeat(201);

        String requestBody = """
            {
                "title": "%s",
                "description": "Testing oversized title",
                "completed": false
            }
            """.formatted(longTitle);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_withMalformedJson_shouldReturn400() throws Exception {
        String brokenJson = "{ this is not valid json }";

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTask_withDuplicateTitle_shouldSucceedWithDifferentIds() throws Exception {
        String requestBody = """
            {
                "title": "Buy milk",
                "description": "First one",
                "completed": false
            }
            """;

        String firstResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Number firstId = com.jayway.jsonpath.JsonPath.read(firstResponse, "$.id");
        Number secondId = com.jayway.jsonpath.JsonPath.read(secondResponse, "$.id");

        assertNotEquals(firstId, secondId);
    }

}