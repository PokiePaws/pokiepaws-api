package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.pokiepaws.api.security.JwtService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

  private JwtService jwtService;
  private UserDetails userDetails;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();
    ReflectionTestUtils.setField(
        jwtService,
        "secretKey",
        "41008870ef465954c6efa46898366c177e7bfe46475376a0459122bd9c63ef51b2a69538fcda053cd6f02cec5ad02dba009f031b39546ba0d0f2b1065288ae9f");
    ReflectionTestUtils.setField(jwtService, "expiration", 60_000L);
    userDetails =
        new User("owner@pokiepaws.pl", "HASH", List.of(new SimpleGrantedAuthority("ROLE_OWNER")));
  }

  @Test
  void generateToken_shouldContainEmailAndRoles() {
    String token = jwtService.generateToken(userDetails);

    assertThat(jwtService.extractEmail(token)).isEqualTo("owner@pokiepaws.pl");
    assertThat(jwtService.extractRoles(token)).containsExactly("ROLE_OWNER");
  }

  @Test
  void isTokenValid_shouldReturnTrueForMatchingUser() {
    String token = jwtService.generateToken(userDetails);

    assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
  }

  @Test
  void isTokenValid_shouldReturnFalseForDifferentUser() {
    String token = jwtService.generateToken(userDetails);
    UserDetails differentUser =
        new User("other@pokiepaws.pl", "HASH", List.of(new SimpleGrantedAuthority("ROLE_OWNER")));

    assertThat(jwtService.isTokenValid(token, differentUser)).isFalse();
  }
}
