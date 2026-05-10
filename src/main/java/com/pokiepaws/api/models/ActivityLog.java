package com.pokiepaws.api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "activity_logs",
        indexes = {
                @Index(name = "idx_log_time", columnList = "time"),
                @Index(name = "idx_log_type", columnList = "type")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LogType type;

    @Column(name = "user_email", nullable = false, length = 160)
    private String userEmail;

    @Column(nullable = false, length = 500)
    private String detail;

    @Column(length = 160)
    private String clinic;

    @Column(nullable = false)
    private LocalDateTime time;

    @PrePersist
    public void prePersist() {
        if (time == null) time = LocalDateTime.now();
    }

    public enum LogType {
        login,
        data,
        supply,
        lab,
        prescription
    }
}