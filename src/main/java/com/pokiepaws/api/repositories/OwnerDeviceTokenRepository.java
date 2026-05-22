package com.pokiepaws.api.repositories;

import com.pokiepaws.api.models.OwnerDeviceToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerDeviceTokenRepository extends JpaRepository<OwnerDeviceToken, Long> {
  List<OwnerDeviceToken> findAllByOwnerUserId(Long ownerUserId);

  Optional<OwnerDeviceToken> findByToken(String token);

  void deleteByOwnerUserIdAndToken(Long ownerUserId, String token);

  void deleteAllByOwnerUserId(Long ownerUserId);
}
