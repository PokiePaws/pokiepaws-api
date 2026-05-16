package com.pokiepaws.api.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pokiepaws.api.controllers.VetController;
import com.pokiepaws.api.exceptions.GlobalExceptionHandler;
import com.pokiepaws.api.models.Vet;
import com.pokiepaws.api.security.JwtService;
import com.pokiepaws.api.security.UserDetailsServiceImpl;
import com.pokiepaws.api.services.VetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VetController.class)
@Import({ControllerSecurityTestConfig.class, GlobalExceptionHandler.class})
class VetControllerSecurityTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean VetService vetService;
  @MockitoBean JwtService jwtService;
  @MockitoBean UserDetailsServiceImpl userDetailsService;

  @Test
  void create_shouldRejectAnonymousUser() throws Exception {
    mockMvc
        .perform(post("/api/vets").contentType(APPLICATION_JSON).content(validVetJson()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "OWNER")
  void create_shouldRejectOwnerRole() throws Exception {
    mockMvc
        .perform(post("/api/vets").contentType(APPLICATION_JSON).content(validVetJson()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void create_shouldReturnVetForAdmin() throws Exception {
    Vet saved =
        Vet.builder()
            .userId(20L)
            .firstName("John")
            .lastName("Smith")
            .npwz("1234567")
            .specialization("Surgery")
            .active(true)
            .build();
    when(vetService.save(any())).thenReturn(saved);

    mockMvc
        .perform(post("/api/vets").contentType(APPLICATION_JSON).content(validVetJson()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(20))
        .andExpect(jsonPath("$.firstName").value("John"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void create_shouldReturn400_whenBodyInvalid() throws Exception {
    mockMvc
        .perform(
            post("/api/vets")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {"firstName":"","lastName":"","npwz":""}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.fields.firstName").exists())
        .andExpect(jsonPath("$.fields.clinicId").exists());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void create_shouldReturn409_whenVetDataConflicts() throws Exception {
    when(vetService.save(any())).thenThrow(new DataIntegrityViolationException("duplicate npwz"));

    mockMvc
        .perform(post("/api/vets").contentType(APPLICATION_JSON).content(validVetJson()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Data conflict"));
  }

  private String validVetJson() {
    return """
        {
          "firstName": "John",
          "lastName": "Smith",
          "phone": "+48123123123",
          "npwz": "1234567",
          "specialization": "Surgery",
          "clinicId": 1
        }
        """;
  }
}
