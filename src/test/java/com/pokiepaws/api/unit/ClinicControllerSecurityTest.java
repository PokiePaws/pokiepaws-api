package com.pokiepaws.api.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pokiepaws.api.controllers.ClinicController;
import com.pokiepaws.api.exceptions.GlobalExceptionHandler;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.security.JwtService;
import com.pokiepaws.api.security.UserDetailsServiceImpl;
import com.pokiepaws.api.services.ClinicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClinicController.class)
@Import({ControllerSecurityTestConfig.class, GlobalExceptionHandler.class})
class ClinicControllerSecurityTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean ClinicService clinicService;
  @MockitoBean JwtService jwtService;
  @MockitoBean UserDetailsServiceImpl userDetailsService;

  @Test
  void create_shouldRejectAnonymousUser() throws Exception {
    mockMvc
        .perform(post("/api/clinics").contentType(APPLICATION_JSON).content(validClinicJson()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "OWNER")
  void create_shouldRejectOwnerRole() throws Exception {
    mockMvc
        .perform(post("/api/clinics").contentType(APPLICATION_JSON).content(validClinicJson()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void create_shouldReturnCreatedClinicForAdmin() throws Exception {
    Clinic saved =
        Clinic.builder()
            .id(10L)
            .clinicName("Test Clinic")
            .regon("987654321")
            .street("Testowa")
            .houseNumber("1")
            .postalCode("59-220")
            .city("Legnica")
            .country("Poland")
            .active(true)
            .build();
    when(clinicService.save(any(Clinic.class))).thenReturn(saved);

    mockMvc
        .perform(post("/api/clinics").contentType(APPLICATION_JSON).content(validClinicJson()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.clinicName").value("Test Clinic"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void create_shouldReturn400_whenBodyInvalid() throws Exception {
    mockMvc
        .perform(
            post("/api/clinics")
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {"clinicName":"","regon":"","city":"Legnica"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.fields.clinicName").exists())
        .andExpect(jsonPath("$.fields.regon").exists())
        .andExpect(jsonPath("$.fields.street").exists());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void create_shouldReturn409_whenClinicDataConflicts() throws Exception {
    when(clinicService.save(any(Clinic.class)))
        .thenThrow(new DataIntegrityViolationException("duplicate regon"));

    mockMvc
        .perform(post("/api/clinics").contentType(APPLICATION_JSON).content(validClinicJson()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Data conflict"));
  }

  private String validClinicJson() {
    return """
        {
          "clinicName": "Test Clinic",
          "regon": "987654321",
          "nip": "9876543210",
          "city": "Legnica",
          "street": "Testowa",
          "houseNumber": "1",
          "postalCode": "59-220",
          "country": "Poland",
          "phone": "123456789",
          "email": "clinic@test.pl",
          "active": true
        }
        """;
  }
}
