package com.company.material.aspect;

import com.company.material.annotation.DataPermission;
import com.company.material.context.DataPermissionContext;
import com.company.material.service.DataPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DataPermissionAspect {

    private final DataPermissionService dataPermissionService;

    @Around("@annotation(dataPermission)")
    public Object around(ProceedingJoinPoint joinPoint, DataPermission dataPermission) throws Throwable {
        DataPermissionContext context = DataPermissionContext.get();
        if (context == null || "ALL".equals(context.getDataScopeType())) {
            return joinPoint.proceed();
        }

        Object result = joinPoint.proceed();
        if (result == null) {
            return result;
        }

        return filterResult(result, dataPermission, context);
    }

    private Object filterResult(Object result, DataPermission dataPermission, DataPermissionContext context) {
        if (result instanceof Page) {
            Page<?> page = (Page<?>) result;
            List<?> filtered = filterList(page.getContent(), dataPermission, context);
            return new org.springframework.data.domain.PageImpl<>(filtered, page.getPageable(), filtered.size());
        } else if (result instanceof List) {
            return filterList((List<?>) result, dataPermission, context);
        }
        return result;
    }

    private <T> List<T> filterList(List<T> list, DataPermission dataPermission, DataPermissionContext context) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        List<T> filtered = new ArrayList<>();
        for (T item : list) {
            if (canAccess(item, dataPermission, context)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private boolean canAccess(Object item, DataPermission dataPermission, DataPermissionContext context) {
        String scopeType = context.getDataScopeType();

        if (dataPermission.checkCreator() && "SELF".equals(scopeType)) {
            Long creatorId = getFieldValue(item, dataPermission.creatorField());
            if (creatorId != null && !creatorId.equals(context.getUserId())) {
                return false;
            }
        }

        if (dataPermission.checkDepartment() && "DEPARTMENT".equals(scopeType)) {
            String department = getFieldValue(item, dataPermission.departmentField());
            if (department != null && context.getDepartment() != null && !department.equals(context.getDepartment())) {
                return false;
            }
        }

        if (dataPermission.checkWarehouse() && "SPECIFIED_WAREHOUSE".equals(scopeType)) {
            Long warehouseId = getFieldValue(item, dataPermission.warehouseField());
            if (warehouseId != null && context.getAccessibleWarehouseIds() != null
                    && !context.getAccessibleWarehouseIds().contains(warehouseId)) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private <T> T getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (Exception e) {
            log.warn("获取字段值失败: {}", fieldName, e);
            return null;
        }
    }
}
