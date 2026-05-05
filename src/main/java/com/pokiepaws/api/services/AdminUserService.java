package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.UserResponse;
import com.pokiepaws.api.models.Role;
import com.pokiepaws.api.models.User;
import com.pokiepaws.api.repositories.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final UserRepository userRepository;

  public List<UserResponse> getAllUsers() {
    return userRepository.findAll().stream().map(UserResponse::from).toList();
  }

  @Transactional
  public UserResponse toggleActive(Long id) {
    User user = findOrThrow(id);
    user.setActive(!user.isActive());
    return UserResponse.from(userRepository.save(user));
  }

  @Transactional
  public UserResponse updateRole(Long id, String roleName) {
    User user = findOrThrow(id);
    try {
      user.setRole(Role.valueOf(roleName.toUpperCase()));
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role: " + roleName);
    }
    return UserResponse.from(userRepository.save(user));
  }

  @Transactional
  public void deleteUser(Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
    }
    userRepository.deleteById(id);
  }

  private User findOrThrow(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }
}
