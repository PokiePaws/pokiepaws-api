package com.pokiepaws.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String street;

    @NotBlank
    private String houseNumber;

    private String apartmentNumber;

    @Pattern(regexp = "\\d{2}-\\d{3}", message = "Postal code must match XX-XXX format")
    @NotBlank
    private String postalCode;

    @NotBlank
    private String city;

    @NotBlank
    private String country;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;
}