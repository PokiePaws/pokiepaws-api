package com.pokiepaws.api.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
    name = "owner_device_tokens",
    uniqueConstraints = {@UniqueConstraint(name = "uk_owner_device_token", columnNames = "token")},
    indexes = {@Index(name = "idx_owner_device_token_owner", columnList = "owner_user_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerDeviceToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_user_id", nullable = false)
  private Owner owner;

  @Column(nullable = false, unique = true, length = 512)
  private String token;

  @Column(length = 32)
  private String platform;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "last_used_at", nullable = false)
  private LocalDateTime lastUsedAt;

  @PrePersist
  void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (lastUsedAt == null) {
      lastUsedAt = now;
    }
  }
}
