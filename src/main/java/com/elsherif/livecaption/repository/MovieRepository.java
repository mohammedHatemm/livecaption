package com.elsherif.livecaption.repository;

import com.elsherif.livecaption.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByTmdbId(Long tmdbId);

    boolean existsByTmdbId(Long tmdbId);

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.voteAverage >= :minRating ORDER BY m.voteAverage DESC")
    List<Movie> findTopRatedMovies(@Param("minRating") java.math.BigDecimal minRating, Pageable pageable);

    List<Movie> findByTmdbIdIn(List<Long> tmdbIds);
}
