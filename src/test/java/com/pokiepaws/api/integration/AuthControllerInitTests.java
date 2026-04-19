package com.pokiepaws.api.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokiepaws.api.dto.AuthRequest;
import com.pokiepaws.api.dto.RegisterRequest;
import com.pokiepaws.api.repositories.EmailVerificationTokenRepository;
import com.pokiepaws.api.repositories.OwnerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class AuthControllerInitTests extends BaseIntegrationTest {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Autowired EmailVerificationTokenRepository tokenRepository;
  @Autowired OwnerRepository ownerRepository;

  @Test
  void register_shouldCreateOwner_verifyEmail_then_login() throws Exception {
    String email = "gabriela@pokiepaws.pl";

    RegisterRequest request = new RegisterRequest();
    request.setFirstName("Gabriela");
    request.setLastName("Grabarska");
    request.setPhoneNumber("+48123123123");
    request.setStreet("Zielona");
    request.setHouseNumber("10");
    request.setApartmentNumber("2");
    request.setPostalCode("59-220");
    request.setCity("Legnica");
    request.setCountry("Poland");
    request.setEmail(email);
    request.setPassword("Owner1234!");

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    var ownerOptional = ownerRepository.findByUserEmail(email);
    assertThat(ownerOptional).isPresent();

    var owner = ownerOptional.get();
    assertThat(owner.getFirstName()).isEqualTo("Gabriela");
    assertThat(owner.getLastName()).isEqualTo("Grabarska");
    assertThat(owner.getPostalCode()).isEqualTo("59-220");
    assertThat(owner.getCity()).isEqualTo("Legnica");
    assertThat(owner.getCountry()).isEqualTo("Poland");

    assertThat(owner.getUser()).isNotNull();
    assertThat(owner.getUserId()).isEqualTo(owner.getUser().getId());

    String verifytoken = tokenRepository.findByUserEmail(email).orElseThrow().getToken();
    mockMvc
        .perform(get("/api/auth/verify-email").param("token", verifytoken))
        .andExpect(status().isOk());

    AuthRequest loginrequest = new AuthRequest();
    loginrequest.setEmail(email);
    loginrequest.setPassword("Owner1234!");

    mockMvc
        .perform(
            post("/api/auth/login")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginrequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").isNotEmpty())
        .andExpect(jsonPath("$.email").value(email))
        .andExpect(jsonPath("$.role").isNotEmpty());
  }
}
