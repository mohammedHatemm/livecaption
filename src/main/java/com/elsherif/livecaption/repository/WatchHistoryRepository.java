package com.elsherif.livecaption.repository;

import com.elsherif.livecaption.entity.WatchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {

    Page<WatchHistory> findByUserIdOrderByLastWatchedAtDesc(Long userId, Pageable pageable);

    Optional<WatchHistory> findByUserIdAndTmdbId(Long userId, Long tmdbId);

    List<WatchHistory> findByUserIdAndCompletedFalseOrderByLastWatchedAtDesc(Long userId, Pageable pageable);

    long countByUserIdAndCompletedTrue(Long userId);

    void deleteByUserIdAndTmdbId(Long userId, Long tmdbId);
}
