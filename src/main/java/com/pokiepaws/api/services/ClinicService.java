package com.pokiepaws.api.services;

import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.repositories.ClinicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClinicService {

    private final ClinicRepository clinicRepository;

    public List<Clinic> getAll() {
        return clinicRepository.findAll();
    }

    public Clinic getById(Long id) {
        return clinicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Clinic not found"));
    }

    public List<Clinic> getByCity(String city) {
        return clinicRepository.findAllByCity(city);
    }

    public Clinic save(Clinic clinic) {
        return clinicRepository.save(clinic);
    }

    public void delete(Long id) {
        clinicRepository.deleteById(id);
    }
}