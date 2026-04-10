package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.ForgotPasswordToken;
import com.pokiepaws.api.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ForgotPasswordTokenRepository extends JpaRepository<ForgotPasswordToken, Long> {

    void deleteAllByUser(User user);

    List<ForgotPasswordToken> findAllByUsedFalseAndExpiresAtAfter(LocalDateTime now);
}