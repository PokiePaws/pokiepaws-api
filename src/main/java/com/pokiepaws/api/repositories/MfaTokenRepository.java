package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.MfaToken;
import com.pokiepaws.api.models.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MfaTokenRepository extends JpaRepository<MfaToken, Long> {

  List<MfaToken> findAllByUserAndUsedFalse(User user);

  List<MfaToken> findAllByUserAndCreatedAtAfter(User user, LocalDateTime createdAfter);

  List<MfaToken> findAllByUsedFalseAndExpiresAtAfter(LocalDateTime now);
}
