package com.pokiepaws.api.services;

import com.pokiepaws.api.dto.warehouse.WarehouseStockItemRequest;
import com.pokiepaws.api.dto.warehouse.WarehouseStockItemResponse;
import com.pokiepaws.api.models.Warehouse;
import com.pokiepaws.api.models.WarehouseStockItem;
import com.pokiepaws.api.repositories.WarehouseRepository;
import com.pokiepaws.api.repositories.WarehouseStockItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class WarehouseStockService {

    private final WarehouseStockItemRepository repository;
    private final WarehouseRepository warehouseRepository;

    public List<WarehouseStockItemResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public List<WarehouseStockItemResponse> getByWarehouse(Long warehouseId) {
        return repository.findAllByWarehouseId(warehouseId).stream().map(this::toResponse).toList();
    }

    public List<WarehouseStockItemResponse> getLowStock(int threshold) {
        return repository.findAllByAmountLessThanEqual(threshold).stream().map(this::toResponse).toList();
    }

    public WarehouseStockItemResponse getById(Long id) {
        return toResponse(findById(id));
    }

    public WarehouseStockItemResponse create(WarehouseStockItemRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse not found"));

        return toResponse(repository.save(WarehouseStockItem.builder()
                .warehouse(warehouse)
                .name(request.getName())
                .assortmentDescription(request.getAssortmentDescription())
                .price(request.getPrice())
                .unit(request.getUnit())
                .category(request.getCategory())
                .amount(request.getAmount())
                .expiryDate(request.getExpiryDate())
                .status(request.getStatus())
                .build()));
    }

    public WarehouseStockItemResponse update(Long id, WarehouseStockItemRequest request) {
        WarehouseStockItem item = findById(id);
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Warehouse not found"));

        item.setWarehouse(warehouse);
        item.setName(request.getName());
        item.setAssortmentDescription(request.getAssortmentDescription());
        item.setPrice(request.getPrice());
        item.setUnit(request.getUnit());
        item.setCategory(request.getCategory());
        item.setAmount(request.getAmount());
        item.setExpiryDate(request.getExpiryDate());
        item.setStatus(request.getStatus());

        return toResponse(repository.save(item));
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }

    private WarehouseStockItem findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stock item not found"));
    }

    private WarehouseStockItemResponse toResponse(WarehouseStockItem item) {
        return WarehouseStockItemResponse.builder()
                .id(item.getId())
                .warehouseId(item.getWarehouse().getId())
                .name(item.getName())
                .assortmentDescription(item.getAssortmentDescription())
                .price(item.getPrice())
                .unit(item.getUnit())
                .category(item.getCategory())
                .amount(item.getAmount())
                .expiryDate(item.getExpiryDate())
                .status(item.getStatus())
                .build();
    }
}