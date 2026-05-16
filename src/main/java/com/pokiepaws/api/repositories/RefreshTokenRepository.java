package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.RefreshToken;
import com.pokiepaws.api.models.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  List<RefreshToken> findAllByRevokedFalseAndExpiresAtAfter(LocalDateTime now);

  List<RefreshToken> findAllByUserAndRevokedFalse(User user);
}
