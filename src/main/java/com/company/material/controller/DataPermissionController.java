package com.company.material.controller;

import com.company.material.annotation.Audit;
import com.company.material.entity.UserDataPermission;
import com.company.material.service.DataPermissionService;
import com.company.material.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data-permissions")
@RequiredArgsConstructor
public class DataPermissionController {

    private final DataPermissionService dataPermissionService;

    private boolean notAdmin(String role) {
        return !"管理员".equals(role);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getPermission(@RequestAttribute("role") String role, @PathVariable Long userId) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可访问"));
        }
        UserDataPermission permission = dataPermissionService.getPermission(userId);
        if (permission == null) {
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "dataScopeType", "SELF",
                    "warehouseIds", List.of()
            ));
        }
        List<Long> warehouseIdList = List.of();
        if (permission.getWarehouseIds() != null && !permission.getWarehouseIds().isBlank()) {
            warehouseIdList = JsonUtil.fromJson(permission.getWarehouseIds(), new TypeReference<List<Long>>() {});
            if (warehouseIdList == null) warehouseIdList = List.of();
        }
        return ResponseEntity.ok(Map.of(
                "id", permission.getId(),
                "userId", permission.getUserId(),
                "dataScopeType", permission.getDataScopeType(),
                "warehouseIds", warehouseIdList,
                "createdAt", permission.getCreatedAt(),
                "updatedAt", permission.getUpdatedAt()
        ));
    }

    @PostMapping("/users/{userId}")
    @Audit(operationType = "配置权限", businessModule = "数据权限")
    public ResponseEntity<?> savePermission(
            @RequestAttribute("role") String role,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可配置"));
        }
        String dataScopeType = (String) body.get("dataScopeType");
        if (dataScopeType == null || !List.of("ALL", "DEPARTMENT", "SELF", "SPECIFIED_WAREHOUSE").contains(dataScopeType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的数据范围类型，可选值：ALL, DEPARTMENT, SELF, SPECIFIED_WAREHOUSE"));
        }
        List<Long> warehouseIds = null;
        if ("SPECIFIED_WAREHOUSE".equals(dataScopeType)) {
            Object warehouseIdsObj = body.get("warehouseIds");
            if (warehouseIdsObj instanceof List) {
                warehouseIds = ((List<?>) warehouseIdsObj).stream()
                        .map(id -> id instanceof Number ? ((Number) id).longValue() : Long.valueOf(id.toString()))
                        .toList();
            }
            if (warehouseIds == null || warehouseIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "指定仓库范围时必须提供仓库ID列表"));
            }
        }
        UserDataPermission saved = dataPermissionService.savePermission(userId, dataScopeType, warehouseIds);
        return ResponseEntity.ok(Map.of(
                "message", "权限配置成功",
                "userId", saved.getUserId(),
                "dataScopeType", saved.getDataScopeType()
        ));
    }

    @DeleteMapping("/users/{userId}")
    @Audit(operationType = "删除权限配置", businessModule = "数据权限")
    public ResponseEntity<?> deletePermission(@RequestAttribute("role") String role, @PathVariable Long userId) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可操作"));
        }
        dataPermissionService.deletePermission(userId);
        return ResponseEntity.ok(Map.of("message", "权限配置已删除，将使用默认权限"));
    }
}
