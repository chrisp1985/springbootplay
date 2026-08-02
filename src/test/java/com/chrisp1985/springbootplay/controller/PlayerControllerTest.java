package com.chrisp1985.springbootplay.controller;

import com.chrisp1985.springbootplay.model.Position;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import com.chrisp1985.springbootplay.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    @Test
    void fetchManualChris_returnsHardcodedPlayer() throws Exception {
        mockMvc.perform(get("/api/v1/players"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {"name":"Chris","age":41,"position":"MF","rating":8.9}
                        """));
    }

    @Test
    void addPlayerToDb_returnsConfirmationMessage() throws Exception {
        FullPlayer saved = new FullPlayer(1L, "Chris", 41, Position.MF, 8.9);
        when(playerService.addPlayerToDatabase(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Chris","age":41,"position":"MF","rating":8.9}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Added player: Chris."));
    }

    @Test
    void addPlayerToDb_rejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","age":41,"position":"MF","rating":8.9}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPlayerDetailsFromAi_returnsServiceResult() throws Exception {
        when(playerService.getPlayerAiInfo(any())).thenReturn("Goals: 10, Assists: 5, Appearances: 20");

        mockMvc.perform(post("/api/v1/players/aidetails")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Chris"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Goals: 10, Assists: 5, Appearances: 20"));
    }
}
