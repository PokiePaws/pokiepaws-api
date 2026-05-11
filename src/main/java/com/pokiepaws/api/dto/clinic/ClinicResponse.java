package com.pokiepaws.api.dto.clinic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicResponse {

    private Long id;
    private String name;
    private String address;
    private String hours;
    private boolean active;
    private String adminName;
}
