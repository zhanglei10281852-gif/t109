package com.company.material.service;

import com.company.material.context.DataPermissionContext;
import com.company.material.entity.User;
import com.company.material.entity.UserDataPermission;
import com.company.material.repository.UserDataPermissionRepository;
import com.company.material.repository.UserRepository;
import com.company.material.util.HttpContextUtil;
import com.company.material.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataPermissionService {

    private final UserDataPermissionRepository permissionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public void loadCurrentUserPermission() {
        Long userId = HttpContextUtil.getCurrentUserId();
        if (userId == null) {
            return;
        }
        String role = HttpContextUtil.getCurrentRole();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        DataPermissionContext context = new DataPermissionContext();
        context.setUserId(userId);
        context.setRole(role);
        context.setDepartment(user.getDepartment());
        if ("管理员".equals(role)) {
            context.setDataScopeType("ALL");
        } else {
            UserDataPermission permission = permissionRepository.findByUserId(userId).orElse(null);
            if (permission != null) {
                context.setDataScopeType(permission.getDataScopeType());
                if (permission.getWarehouseIds() != null && !permission.getWarehouseIds().isBlank()) {
                    List<Long> warehouseIds = parseWarehouseIds(permission.getWarehouseIds());
                    context.setAccessibleWarehouseIds(warehouseIds);
                }
            } else {
                context.setDataScopeType("SELF");
            }
        }
        DataPermissionContext.set(context);
    }

    private List<Long> parseWarehouseIds(String warehouseIdsStr) {
        try {
            if (warehouseIdsStr.startsWith("[")) {
                return JsonUtil.fromJson(warehouseIdsStr, new TypeReference<List<Long>>() {});
            } else {
                return Arrays.stream(warehouseIdsStr.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Transactional
    public UserDataPermission savePermission(Long userId, String dataScopeType, List<Long> warehouseIds) {
        UserDataPermission permission = permissionRepository.findByUserId(userId).orElse(null);
        if (permission == null) {
            permission = new UserDataPermission();
            permission.setUserId(userId);
        }
        permission.setDataScopeType(dataScopeType);
        if ("SPECIFIED_WAREHOUSE".equals(dataScopeType) && warehouseIds != null && !warehouseIds.isEmpty()) {
            permission.setWarehouseIds(JsonUtil.toJson(warehouseIds));
        } else {
            permission.setWarehouseIds(null);
        }
        return permissionRepository.save(permission);
    }

    @Transactional(readOnly = true)
    public UserDataPermission getPermission(Long userId) {
        return permissionRepository.findByUserId(userId).orElse(null);
    }

    @Transactional
    public void deletePermission(Long userId) {
        permissionRepository.deleteByUserId(userId);
    }

    public boolean canAccessWarehouse(Long warehouseId) {
        DataPermissionContext context = DataPermissionContext.get();
        if (context == null) {
            return false;
        }
        String scopeType = context.getDataScopeType();
        if ("ALL".equals(scopeType)) {
            return true;
        }
        if ("SPECIFIED_WAREHOUSE".equals(scopeType)) {
            List<Long> accessibleIds = context.getAccessibleWarehouseIds();
            return accessibleIds != null && accessibleIds.contains(warehouseId);
        }
        return false;
    }

    public boolean canAccessDepartment(String department) {
        DataPermissionContext context = DataPermissionContext.get();
        if (context == null) {
            return false;
        }
        String scopeType = context.getDataScopeType();
        if ("ALL".equals(scopeType)) {
            return true;
        }
        if ("DEPARTMENT".equals(scopeType)) {
            return context.getDepartment() != null && context.getDepartment().equals(department);
        }
        return false;
    }

    public boolean canAccessCreator(Long creatorId) {
        DataPermissionContext context = DataPermissionContext.get();
        if (context == null) {
            return false;
        }
        String scopeType = context.getDataScopeType();
        if ("ALL".equals(scopeType)) {
            return true;
        }
        if ("SELF".equals(scopeType)) {
            return context.getUserId() != null && context.getUserId().equals(creatorId);
        }
        return false;
    }
}
