package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.warehouse.ClinicAssortmentItemRequest;
import com.pokiepaws.api.dto.warehouse.ClinicAssortmentItemResponse;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.ClinicAssortmentItem;
import com.pokiepaws.api.repositories.ClinicAssortmentItemRepository;
import com.pokiepaws.api.repositories.ClinicRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ClinicAssortmentService {

    private final ClinicAssortmentItemRepository repository;
    private final ClinicRepository clinicRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<ClinicAssortmentItemResponse> getAll(Long clinicId, String status) {
        if (clinicId != null && status != null)
            return repository.findAllByClinicIdAndStatus(clinicId, status).stream()
                    .map(this::toResponse)
                    .toList();
        if (clinicId != null)
            return repository.findAllByClinicId(clinicId).stream().map(this::toResponse).toList();
        if (status != null)
            return repository.findAllByStatus(status).stream().map(this::toResponse).toList();
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public ClinicAssortmentItemResponse create(ClinicAssortmentItemRequest request) {
        Clinic clinic =
                clinicRepository
                        .findById(request.getClinicId())
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Clinic not found"));

        ClinicAssortmentItem item =
                ClinicAssortmentItem.builder()
                        .clinic(clinic)
                        .name(request.getName())
                        .amount(request.getAmount())
                        .description(request.getDescription())
                        .category(request.getCategory())
                        .unit(request.getUnit())
                        .expiryDate(request.getExpiryDate())
                        .build();

        ClinicAssortmentItemResponse response = toResponse(repository.save(item));
        messagingTemplate.convertAndSend("/topic/orders", response);
        return response;
    }

    public ClinicAssortmentItemResponse updateStatus(Long id, String status) {
        ClinicAssortmentItem item =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Order not found"));
        item.setStatus(status);
        return toResponse(repository.save(item));
    }

    private ClinicAssortmentItemResponse toResponse(ClinicAssortmentItem item) {
        return ClinicAssortmentItemResponse.builder()
                .id(item.getId())
                .clinicId(item.getClinic().getId())
                .clinicName(item.getClinic().getClinicName())
                .name(item.getName())
                .amount(item.getAmount())
                .description(item.getDescription())
                .category(item.getCategory())
                .status(item.getStatus())
                .unit(item.getUnit())
                .expiryDate(item.getExpiryDate())
                .build();
    }
}