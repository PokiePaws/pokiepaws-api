package com.pokiepaws.api.controllers;

import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.services.ClinicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clinics")
@RequiredArgsConstructor
public class ClinicController {

    private final ClinicService clinicService;

    @GetMapping
    public List<Clinic> getAll() {
        return clinicService.getAll();
    }

    @GetMapping("/{id}")
    public Clinic getById(@PathVariable Long id) {
        return clinicService.getById(id);
    }

    @GetMapping("/city/{city}")
    public List<Clinic> getByCity(@PathVariable String city) {
        return clinicService.getByCity(city);
    }

    @PostMapping
    public Clinic create(@RequestBody Clinic clinic) {
        return clinicService.save(clinic);
    }

    @PutMapping("/{id}")
    public Clinic update(@PathVariable Long id, @RequestBody Clinic clinic) {
        clinic.setId(id);
        return clinicService.save(clinic);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        clinicService.delete(id);
    }
}