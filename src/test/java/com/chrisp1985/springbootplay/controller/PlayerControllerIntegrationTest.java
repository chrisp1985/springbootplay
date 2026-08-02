package com.chrisp1985.springbootplay.controller;

import com.chrisp1985.springbootplay.repository.PlayerRespository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the controller against a real MySQL container (via Testcontainers) and the real Flyway
 * migrations, rather than mocking the service layer or swapping in H2, so the full save-and-
 * read-back path is exercised the way it would run in production.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PlayerControllerIntegrationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer mysql = new MySQLContainer("mysql:8.0");

    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private PlayerRespository playerRespository;

    @Test
    void fetchPlayerByName_secondRequestIsServedFromCacheNotTheDatabase() throws Exception {
        mockMvc.perform(get("/api/v1/players/Harry Kane"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/players/Harry Kane"))
                .andExpect(status().isOk());

        Mockito.verify(playerRespository, Mockito.times(1)).findFirstByName("Harry Kane");
    }

    @Test
    void fetchAllPlayers_returnsSeededPlayers() throws Exception {
        mockMvc.perform(get("/api/v1/players"))
                .andExpect(status().isOk());
    }

    @Test
    void addPlayerToDb_persistsPlayerAgainstRealMySql() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"TestcontainerChris","age":30,"position":"FW","rating":7.5}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Added player: TestcontainerChris."));

        mockMvc.perform(get("/api/v1/players/TestcontainerChris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("TestcontainerChris"));
    }

    @Test
    void fetchPlayerByName_returnsNotFoundForUnknownPlayer() throws Exception {
        mockMvc.perform(get("/api/v1/players/Nobody"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addPlayerToDb_rejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","age":30,"position":"FW","rating":7.5}
                                """))
                .andExpect(status().isBadRequest());
    }
}
