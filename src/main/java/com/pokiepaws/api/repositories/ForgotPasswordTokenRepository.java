package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.ForgotPasswordToken;
import com.pokiepaws.api.models.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgotPasswordTokenRepository extends JpaRepository<ForgotPasswordToken, Long> {

  void deleteAllByUser(User user);

  List<ForgotPasswordToken> findAllByUsedFalseAndExpiresAtAfter(LocalDateTime now);
}
