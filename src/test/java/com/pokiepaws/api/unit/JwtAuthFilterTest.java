package com.pokiepaws.api.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pokiepaws.api.security.JwtAuthFilter;
import com.pokiepaws.api.security.JwtService;
import com.pokiepaws.api.security.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

  @Mock JwtService jwtService;
  @Mock UserDetailsServiceImpl userDetailsService;
  @Mock FilterChain filterChain;

  private JwtAuthFilter jwtAuthFilter;

  @BeforeEach
  void setUp() {
    jwtAuthFilter = new JwtAuthFilter(jwtService, userDetailsService);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void doFilterInternal_shouldContinueWithoutAuthenticationWhenHeaderIsMissing() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    jwtAuthFilter.doFilter(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(jwtService, never()).extractEmail("TOKEN");
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_shouldSetAuthenticationWhenTokenIsValid() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer TOKEN");
    MockHttpServletResponse response = new MockHttpServletResponse();
    User userDetails =
        new User("owner@pokiepaws.pl", "HASH", List.of(new SimpleGrantedAuthority("ROLE_OWNER")));

    when(jwtService.extractEmail("TOKEN")).thenReturn("owner@pokiepaws.pl");
    when(userDetailsService.loadUserByUsername("owner@pokiepaws.pl")).thenReturn(userDetails);
    when(jwtService.isTokenValid("TOKEN", userDetails)).thenReturn(true);
    when(jwtService.extractRoles("TOKEN")).thenReturn(List.of("ROLE_OWNER"));

    jwtAuthFilter.doFilter(request, response, filterChain);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.getName()).isEqualTo("owner@pokiepaws.pl");
    assertThat(authentication.getAuthorities())
        .extracting("authority")
        .containsExactly("ROLE_OWNER");
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void doFilterInternal_shouldReturn401AndClearContextWhenTokenIsInvalid() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer BAD");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(jwtService.extractEmail("BAD")).thenThrow(new RuntimeException("bad token"));

    jwtAuthFilter.doFilter(request, response, filterChain);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentAsString()).contains("Invalid or expired JWT");
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain, never()).doFilter(request, response);
  }
}
