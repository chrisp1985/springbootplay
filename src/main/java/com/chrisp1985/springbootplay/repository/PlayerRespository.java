package com.chrisp1985.springbootplay.repository;

import com.chrisp1985.springbootplay.model.entity.FullPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRespository extends JpaRepository<FullPlayer, Long> {

    /**
     * Names aren't unique (e.g. multiple "TRIALIST" entries), so this returns every match
     * rather than erroring. Used by flows that must act on all players sharing a name.
     */
    List<FullPlayer> findAllByName(String name);

    Optional<FullPlayer> findFirstByName(String name);

    /**
     * Rating refresh and AI-summary enrichment each load, mutate, and persist this row
     * independently and can interleave (one from the scheduled sweep, one from an in-flight AI
     * call). A plain {@code save()} goes through {@code entityManager.merge()}, which copies
     * every field of whichever copy was loaded, so a save from the "other" flow can silently
     * overwrite this field back to a stale value even though it never touched it directly.
     * These narrow, single-purpose updates only ever touch their own column(s), so the two
     * flows can never clobber each other no matter how they interleave.
     */
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE FullPlayer p SET p.rating = :rating, p.updatedAt = :updatedAt WHERE p.id = :id")
    void updateRating(@Param("id") Long id, @Param("rating") Double rating, @Param("updatedAt") Instant updatedAt);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE FullPlayer p SET p.aiSummary = :aiSummary WHERE p.id = :id")
    void updateAiSummary(@Param("id") Long id, @Param("aiSummary") String aiSummary);

}
