package com.elsherif.livecaption.repository;

import com.elsherif.livecaption.entity.Watchlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    Page<Watchlist> findByUserIdOrderByAddedAtDesc(Long userId, Pageable pageable);

    Optional<Watchlist> findByUserIdAndTmdbId(Long userId, Long tmdbId);

    boolean existsByUserIdAndTmdbId(Long userId, Long tmdbId);

    void deleteByUserIdAndTmdbId(Long userId, Long tmdbId);

    long countByUserId(Long userId);
}
