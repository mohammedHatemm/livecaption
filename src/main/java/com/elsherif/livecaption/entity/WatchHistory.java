package com.elsherif.livecaption.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "watch_history", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "tmdb_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "tmdb_id", nullable = false)
    private Long tmdbId;

    @Column(name = "progress_seconds")
    @Builder.Default
    private Integer progressSeconds = 0;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Builder.Default
    private Boolean completed = false;

    @CreationTimestamp
    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;

    @UpdateTimestamp
    @Column(name = "last_watched_at")
    private LocalDateTime lastWatchedAt;
}
