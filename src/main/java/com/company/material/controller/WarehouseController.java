package com.company.material.controller;

import com.company.material.annotation.Audit;
import com.company.material.annotation.DataPermission;
import com.company.material.annotation.SensitiveOperation;
import com.company.material.context.DataPermissionContext;
import com.company.material.entity.Warehouse;
import com.company.material.repository.WarehouseRepository;
import com.company.material.util.HttpContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseRepository warehouseRepository;

    @PostMapping
    @Audit(operationType = "创建", businessModule = "仓库", entityClass = Warehouse.class)
    public ResponseEntity<?> create(@RequestBody Warehouse warehouse) {
        if (warehouse.getWarehouseCode() == null || warehouse.getName() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "仓库编码和名称为必填"));
        }
        if (warehouseRepository.existsByWarehouseCode(warehouse.getWarehouseCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "仓库编码已存在"));
        }
        warehouse.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseRepository.save(warehouse));
    }

    @GetMapping
    @DataPermission(checkWarehouse = true, checkCreator = true)
    public ResponseEntity<?> list(@RequestParam(required = false) String status) {
        List<Warehouse> result;
        DataPermissionContext context = DataPermissionContext.get();

        if (context != null && "SPECIFIED_WAREHOUSE".equals(context.getDataScopeType())) {
            List<Long> accessibleIds = context.getAccessibleWarehouseIds();
            if (accessibleIds == null || accessibleIds.isEmpty()) {
                return ResponseEntity.ok(new ArrayList<>());
            }
            if (status != null && !status.isBlank()) {
                result = warehouseRepository.findByIds(accessibleIds).stream()
                        .filter(w -> status.equals(w.getStatus()))
                        .toList();
            } else {
                result = warehouseRepository.findByIds(accessibleIds);
            }
        } else if (context != null && "SELF".equals(context.getDataScopeType())) {
            Long userId = HttpContextUtil.getCurrentUserId();
            if (status != null && !status.isBlank()) {
                result = warehouseRepository.findByCreatedBy(userId).stream()
                        .filter(w -> status.equals(w.getStatus()))
                        .toList();
            } else {
                result = warehouseRepository.findByCreatedBy(userId);
            }
        } else {
            if (status != null && !status.isBlank()) {
                result = warehouseRepository.findByStatus(status);
            } else {
                result = warehouseRepository.findAll();
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return warehouseRepository.findById(id)
                .map(w -> ResponseEntity.ok((Object) w))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Audit(operationType = "修改", businessModule = "仓库", entityClass = Warehouse.class)
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Warehouse body) {
        return warehouseRepository.findById(id).map(w -> {
            if (body.getName() != null) w.setName(body.getName());
            if (body.getLocation() != null) w.setLocation(body.getLocation());
            if (body.getManager() != null) w.setManager(body.getManager());
            if (body.getPhone() != null) w.setPhone(body.getPhone());
            if (body.getStatus() != null) w.setStatus(body.getStatus());
            return ResponseEntity.ok((Object) warehouseRepository.save(w));
        }).orElse(ResponseEntity.notFound().build());
    }
}
