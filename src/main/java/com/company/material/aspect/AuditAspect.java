package com.company.material.aspect;

import com.company.material.annotation.Audit;
import com.company.material.service.AuditLogService;
import com.company.material.util.JsonUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final EntityManager entityManager;

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        Long targetId = extractTargetId(paramNames, args, audit.targetIdParam());
        String targetName = null;
        Object beforeSnapshot = null;

        if (targetId != null && audit.entityClass() != Object.class) {
            beforeSnapshot = entityManager.find(audit.entityClass(), targetId);
            if (beforeSnapshot != null) {
                targetName = extractNameField(beforeSnapshot);
            }
        }

        if (beforeSnapshot == null && "创建".equals(audit.operationType())) {
            for (Object arg : args) {
                if (arg != null && !arg.getClass().isPrimitive() && !arg.getClass().getName().startsWith("java.")) {
                    targetName = extractNameField(arg);
                    break;
                }
            }
        }

        Object result = null;
        boolean success = true;
        String errorMessage = null;
        Object afterSnapshot = null;

        try {
            result = joinPoint.proceed();
            if (targetId == null && result != null) {
                targetId = extractIdFromResult(result);
            }
            if (targetId != null && audit.entityClass() != Object.class) {
                entityManager.clear();
                afterSnapshot = entityManager.find(audit.entityClass(), targetId);
                if (targetName == null && afterSnapshot != null) {
                    targetName = extractNameField(afterSnapshot);
                }
            }
        } catch (Throwable t) {
            success = false;
            errorMessage = t.getMessage();
            throw t;
        } finally {
            String description = audit.description();
            if (description.isEmpty()) {
                description = buildDefaultDescription(audit.operationType(), audit.businessModule(), targetName);
            }
            auditLogService.logOperation(
                    audit.operationType(),
                    audit.businessModule(),
                    targetId,
                    targetName,
                    description,
                    beforeSnapshot,
                    afterSnapshot != null ? afterSnapshot : result,
                    success,
                    errorMessage
            );
        }
        return result;
    }

    private Long extractTargetId(String[] paramNames, Object[] args, String targetIdParam) {
        for (int i = 0; i < paramNames.length; i++) {
            if (targetIdParam.equals(paramNames[i]) && args[i] instanceof Long) {
                return (Long) args[i];
            }
        }
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
            if (arg instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) arg;
                Object id = map.get("id");
                if (id instanceof Number) {
                    return ((Number) id).longValue();
                }
            }
        }
        return null;
    }

    private Long extractIdFromResult(Object result) {
        try {
            if (result instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) result;
                Object id = map.get("id");
                if (id instanceof Number) {
                    return ((Number) id).longValue();
                }
            }
            Field idField = result.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(result);
            if (id instanceof Number) {
                return ((Number) id).longValue();
            }
        } catch (Exception e) {
        }
        return null;
    }

    private String extractNameField(Object entity) {
        try {
            Field[] fields = entity.getClass().getDeclaredFields();
            for (Field field : fields) {
                String name = field.getName().toLowerCase();
                if (name.contains("name") || name.equals("username") || name.equals("realname")) {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    if (value != null) {
                        return value.toString();
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

    private String buildDefaultDescription(String operationType, String businessModule, String targetName) {
        StringBuilder sb = new StringBuilder();
        sb.append(operationType).append(businessModule);
        if (targetName != null) {
            sb.append("[").append(targetName).append("]");
        }
        return sb.toString();
    }
}
