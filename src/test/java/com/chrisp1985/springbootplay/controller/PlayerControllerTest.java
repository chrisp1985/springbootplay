package com.chrisp1985.springbootplay.controller;

import com.chrisp1985.springbootplay.exception.PlayerNotFoundException;
import com.chrisp1985.springbootplay.model.Position;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import com.chrisp1985.springbootplay.service.PlayerEnrichmentService;
import com.chrisp1985.springbootplay.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @EnableCaching lives on the main application class, so this slice picks it up too, but
 * @WebMvcTest doesn't autoconfigure a CacheManager (it's not web-layer related) — without this
 * import, the caching aspect on PlayerService fails to start with no CacheManager bean found.
 */
@ImportAutoConfiguration(CacheAutoConfiguration.class)
@WebMvcTest(PlayerController.class)
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlayerService playerService;

    @MockitoBean
    private PlayerEnrichmentService playerEnrichmentService;

    @Test
    void fetchAllPlayers_returnsPageOfPlayersFromService() throws Exception {
        FullPlayer chris = new FullPlayer(1L, "Chris", 41, Position.MF, 8.9);
        when(playerService.getAllPlayers(any(), any())).thenReturn(new PageImpl<>(List.of(chris)));

        mockMvc.perform(get("/api/v1/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Chris"));
    }

    @Test
    void fetchPlayerByName_returnsPlayerFromService() throws Exception {
        FullPlayer chris = new FullPlayer(1L, "Chris", 41, Position.MF, 8.9);
        when(playerService.getPlayerByName("Chris")).thenReturn(chris);

        mockMvc.perform(get("/api/v1/players/Chris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Chris"));
    }

    @Test
    void fetchPlayerByName_returnsNotFoundWhenMissing() throws Exception {
        when(playerService.getPlayerByName("Unknown")).thenThrow(new PlayerNotFoundException("Unknown"));

        mockMvc.perform(get("/api/v1/players/Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addPlayerToDb_returnsCreatedPlayerAndTriggersEnrichment() throws Exception {
        FullPlayer saved = new FullPlayer(1L, "Chris", 41, Position.MF, 8.9);
        when(playerService.addPlayerToDatabase(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Chris","age":41,"position":"MF","rating":8.9}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Chris"))
                .andExpect(jsonPath("$.age").value(41))
                .andExpect(jsonPath("$.position").value("MF"))
                .andExpect(jsonPath("$.rating").value(8.9));

        verify(playerEnrichmentService).enrichPlayer("Chris");
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
    void enrichPlayer_triggersEnrichmentService() throws Exception {
        mockMvc.perform(post("/api/v1/players/Chris/enrich"))
                .andExpect(status().isOk())
                .andExpect(content().string("Enrichment started for: Chris."));

        verify(playerEnrichmentService).enrichPlayer("Chris");
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
                .andExpect(jsonPath("$.name").value("Chris"))
                .andExpect(jsonPath("$.summary").value("Goals: 10, Assists: 5, Appearances: 20"));
    }
}
