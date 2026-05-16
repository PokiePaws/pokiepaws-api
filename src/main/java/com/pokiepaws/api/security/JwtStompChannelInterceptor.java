package com.pokiepaws.api.security;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtStompChannelInterceptor implements ChannelInterceptor {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;
  private final UserDetailsServiceImpl userDetailsService;

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null || accessor.getCommand() == null) {
      return message;
    }

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      authenticateConnection(accessor);
    }

    if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
      authorizeSubscription(accessor);
    }

    return message;
  }

  private void authenticateConnection(StompHeaderAccessor accessor) {
    String authHeader = accessor.getFirstNativeHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      throw new AuthenticationCredentialsNotFoundException("Missing WebSocket JWT");
    }

    String token = authHeader.substring(BEARER_PREFIX.length());
    String email = jwtService.extractEmail(token);
    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

    if (!jwtService.isTokenValid(token, userDetails)) {
      throw new AuthenticationCredentialsNotFoundException("Invalid WebSocket JWT");
    }

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    accessor.setUser(authentication);
  }

  private void authorizeSubscription(StompHeaderAccessor accessor) {
    if (!(accessor.getUser() instanceof UsernamePasswordAuthenticationToken authentication)) {
      throw new AccessDeniedException("WebSocket subscription requires authentication");
    }

    String destination = accessor.getDestination();
    if (destination == null) {
      return;
    }

    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

    if (destination.startsWith("/topic/admin/") && !hasRole(authorities, "ROLE_ADMIN")) {
      throw new AccessDeniedException("Admin WebSocket topic requires ADMIN role");
    }

    if (destination.startsWith("/topic/warehouse/")
        && !hasAnyRole(authorities, "ROLE_ADMIN", "ROLE_WAREHOUSE")) {
      throw new AccessDeniedException("Warehouse WebSocket topic requires WAREHOUSE role");
    }

    if (destination.startsWith("/topic/clinics/")
        && !hasAnyRole(authorities, "ROLE_ADMIN", "ROLE_VET", "ROLE_WAREHOUSE")) {
      throw new AccessDeniedException("Clinic WebSocket topic requires clinic staff role");
    }
  }

  private boolean hasAnyRole(Collection<? extends GrantedAuthority> authorities, String... roles) {
    for (String role : roles) {
      if (hasRole(authorities, role)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasRole(Collection<? extends GrantedAuthority> authorities, String role) {
    return authorities.stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
  }
}
