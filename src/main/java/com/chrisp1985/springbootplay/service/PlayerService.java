package com.chrisp1985.springbootplay.service;

import com.chrisp1985.springbootplay.exception.PlayerNotFoundException;
import com.chrisp1985.springbootplay.model.*;
import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import com.chrisp1985.springbootplay.model.mapper.AuditMapper;
import com.chrisp1985.springbootplay.model.mapper.PlayerMapper;
import com.chrisp1985.springbootplay.repository.AuditLogRespository;
import com.anthropic.models.messages.OutputConfig;
import com.chrisp1985.springbootplay.repository.PlayerRespository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);
    private final ChatClient chatClient;
    private final PlayerRespository playerRespository;
    private final AuditLogRespository auditLogRespository;
    private final PlayerMapper playerMapper;
    private final AuditMapper auditMapper;
    private final MeterRegistry meterRegistry;

    @Autowired
    public PlayerService(ChatClient.Builder chatClientBuilder, PlayerMapper playerMapper,
                         AuditMapper auditMapper, PlayerRespository playerRespository, AuditLogRespository auditLogRespository,
                         MeterRegistry meterRegistry) {
        this.chatClient = chatClientBuilder.build();
        this.playerMapper = playerMapper;
        this.playerRespository = playerRespository;
        this.auditLogRespository = auditLogRespository;
        this.auditMapper = auditMapper;
        this.meterRegistry = meterRegistry;
    }

    /**
     * When thinking is on (the default for Claude Opus 5 unless disabled), Spring AI's Anthropic
     * client surfaces the thinking block and the text answer as separate {@link Generation}s
     * rather than combining them into one — {@code chatResponse().getResult()} (what
     * {@code .content()} uses under the hood) is only the *first* of these, which is the
     * thinking block with empty text. Pulling the text from every generation and joining the
     * non-blank ones picks up the actual answer regardless of how many blocks the response split
     * into.
     */
    public String getPlayerAiInfo(PlayerDetailsRequest player) {
        var chatResponse = chatClient
                .prompt("Return " + player.name() +
                        "'s most notable historical football season (from your training data, not live data) " +
                        "as plain text. State which season it is, and include goals, assists, appearances.")
                .options(AnthropicChatOptions.builder().effort(OutputConfig.Effort.LOW).build())
                .call()
                .chatResponse();
        if (chatResponse == null) {
            return null;
        }
        return chatResponse.getResults().stream()
                .map(Generation::getOutput)
                .map(AssistantMessage::getText)
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.joining());
    }

    /**
     * Ratings refresh every 5 minutes via the scheduled sweep and enrichment writes bypass this
     * cache entirely (they go straight to the DB via narrow update queries), so a cached page
     * can show a stale rating/AI summary for up to the cache's expireAfterWrite window. That's
     * an accepted tradeoff for a read path that's otherwise hit on every page load.
     */
    @Cacheable(cacheNames = "players")
    public Page<FullPlayer> getAllPlayers(Pageable pageable) {
        return timeDbCall("findAll", () -> playerRespository.findAll(pageable));
    }

    @Cacheable(cacheNames = "player", key = "#name")
    public FullPlayer getPlayerByName(String name) {
        return timeDbCall("findByName", () -> playerRespository.findFirstByName(name))
                .orElseThrow(() -> new PlayerNotFoundException(name));
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "players", allEntries = true),
            @CacheEvict(cacheNames = "player", key = "#playerDto.name()")
    })
    @Transactional
    public FullPlayer addPlayerToDatabase(PlayerRequest playerDto) {
        FullPlayer addedPlayer = timeDbCall("save", () -> playerRespository.save(playerMapper.toEntity(playerDto)));
        log.info("Saving data: {}", playerDto);
        timeDbCall("auditSave", () -> auditLogRespository.save(auditMapper.toEntity(playerDto)));
        return addedPlayer;
    }

    /**
     * Wraps a repository call with a `db.latency` timer, tagged by operation, so the round trip
     * from this service to the database shows up in /actuator/metrics rather than only being
     * visible in a profiler.
     */
    private <T> T timeDbCall(String operation, Supplier<T> call) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            return call.get();
        } finally {
            sample.stop(Timer.builder("db.latency")
                    .description("Latency of calls from PlayerService to the database")
                    .tag("operation", operation)
                    .register(meterRegistry));
        }
    }
}
