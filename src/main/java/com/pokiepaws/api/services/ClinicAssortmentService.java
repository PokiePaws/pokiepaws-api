package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.warehouse.ClinicAssortmentItemRequest;
import com.pokiepaws.api.dto.warehouse.ClinicAssortmentItemResponse;
import com.pokiepaws.api.models.Clinic;
import com.pokiepaws.api.models.ClinicAssortmentItem;
import com.pokiepaws.api.models.WarehouseStockItem;
import com.pokiepaws.api.repositories.ClinicAssortmentItemRepository;
import com.pokiepaws.api.repositories.ClinicRepository;
import com.pokiepaws.api.repositories.WarehouseStockItemRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClinicAssortmentService {

    private final ClinicAssortmentItemRepository repository;
    private final ClinicRepository clinicRepository;
    private final WarehouseStockItemRepository warehouseStockItemRepository;
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
                                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Clinic not found"));

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

    @Transactional
    public ClinicAssortmentItemResponse updateStatus(Long id, String status) {
        ClinicAssortmentItem item =
                repository
                        .findById(id)
                        .orElseThrow(
                                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        item.setStatus(status);
        ClinicAssortmentItem saved = repository.save(item);

        if ("SHIPPED".equals(status)) {
            decrementStock(item.getName(), item.getAmount());
        }

        return toResponse(saved);
    }

    private void decrementStock(String productName, int orderedAmount) {
        Optional<WarehouseStockItem> stockOpt = warehouseStockItemRepository.findByNameIgnoreCase(productName);
        if (stockOpt.isEmpty()) {
            log.warn("No warehouse stock item found for name '{}', skipping stock decrement.", productName);
            return;
        }

        WarehouseStockItem stock = stockOpt.get();
        int newAmount = Math.max(0, stock.getAmount() - orderedAmount);
        stock.setAmount(newAmount);

        if (newAmount == 0) {
            stock.setStatus("OUT_OF_STOCK");
        } else if (newAmount <= 10) {
            stock.setStatus("LOW_STOCK");
        } else {
            stock.setStatus("AVAILABLE");
        }

        warehouseStockItemRepository.save(stock);
        log.info("Stock decremented for '{}': {} -> {}", productName, stock.getAmount() + orderedAmount, newAmount);
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