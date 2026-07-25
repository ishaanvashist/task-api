package com.ishu.task_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest                          // boots your real, full Spring app for this test — no mocking
@AutoConfigureMockMvc                    // gives us a MockMvc object to simulate real HTTP calls
public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;              // simulates real HTTP requests, no real server actually started

    @Test
    void getAllTasks_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/tasks"))             // real GET request to /api/tasks
                .andExpect(status().isOk());           // expect 200
    }

    @Test
    void createTask_withValidData_shouldReturn201() throws Exception {
        String requestBody = """
                {
                    "title": "Buy groceries",
                    "description": "Milk, eggs, bread",
                    "completed": false
                }
                """;                                    // real JSON, same as a real client would send

        mockMvc.perform(post("/api/tasks")              // real POST request
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());        // expect 201, from the @ResponseStatus fix
    }

    @Test
    void createTask_withBlankTitle_shouldReturn400() throws Exception {
        String requestBody = """
                {
                    "title": "",
                    "description": "Missing title",
                    "completed": false
                }
                """;                                     // blank title — should trigger @NotBlank

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());      // expect 400
    }

    @Test
    void getTaskById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/tasks/9999"))           // id that doesn't exist
                .andExpect(status().isNotFound());        // expect 404
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

        String response = mockMvc.perform(post("/api/tasks")   // create a real task first
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andReturn()
                .getResponse()
                .getContentAsString();                          // get the real JSON response back

        Number createdId = com.jayway.jsonpath.JsonPath.read(response, "$.id"); // pull "id" out of that JSON

        mockMvc.perform(delete("/api/tasks/" + createdId))      // delete that real task
                .andExpect(status().isNoContent());              // expect 204, from the @ResponseStatus fix
    }

    @Test
    void createTask_withOversizedTitle_shouldReturn400() throws Exception {
        String longTitle = "a".repeat(201);              // 201 chars — 1 over your 200 limit

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
                .andExpect(status().isBadRequest());      // expect 400, from the new @Size constraint
    }

    @Test
    void createTask_withMalformedJson_shouldReturn400() throws Exception {
        String brokenJson = "{ this is not valid json }";  // deliberately broken

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(brokenJson))
                .andExpect(status().isBadRequest());        // Spring's default behavior for unparseable JSON
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

        // create first task
        String firstResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // create second task, same title
        String secondResponse = mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())            // both succeed — duplicates are valid
                .andReturn().getResponse().getContentAsString();

        Number firstId = com.jayway.jsonpath.JsonPath.read(firstResponse, "$.id");
        Number secondId = com.jayway.jsonpath.JsonPath.read(secondResponse, "$.id");

        assertNotEquals(firstId, secondId);                 // proves each gets its own real id
    }

}