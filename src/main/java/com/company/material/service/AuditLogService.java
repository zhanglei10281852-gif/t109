package com.company.material.service;

import com.company.material.entity.AuditLog;
import com.company.material.repository.AuditLogRepository;
import com.company.material.util.HttpContextUtil;
import com.company.material.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOperation(String operationType, String businessModule,
                             Long targetId, String targetName, String description,
                             Object beforeSnapshot, Object afterSnapshot,
                             boolean success, String errorMessage) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setOperatorId(HttpContextUtil.getCurrentUserId());
            auditLog.setOperatorName(HttpContextUtil.getCurrentUsername());
            auditLog.setOperatorRole(HttpContextUtil.getCurrentRole());
            auditLog.setOperationType(operationType);
            auditLog.setBusinessModule(businessModule);
            auditLog.setTargetId(targetId);
            auditLog.setTargetName(targetName);
            auditLog.setDescription(description);
            auditLog.setBeforeSnapshot(JsonUtil.toJson(beforeSnapshot));
            auditLog.setAfterSnapshot(JsonUtil.toJson(afterSnapshot));
            auditLog.setRequestIp(HttpContextUtil.getClientIp());
            auditLog.setOperationTime(LocalDateTime.now());
            auditLog.setResult(success ? "成功" : "失败");
            auditLog.setErrorMessage(errorMessage);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("记录审计日志失败", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> search(Long operatorId, String businessModule, String operationType,
                                 String result, LocalDateTime startTime, LocalDateTime endTime,
                                 Pageable pageable) {
        return auditLogRepository.findByConditions(operatorId, businessModule, operationType,
                result, startTime, endTime, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<AuditLog> findById(Long id) {
        return auditLogRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> compareSnapshots(Long id) {
        return auditLogRepository.findById(id).map(auditLog -> {
            String before = auditLog.getBeforeSnapshot();
            String after = auditLog.getAfterSnapshot();
            if (before == null || after == null) {
                return Collections.<Map<String, Object>>emptyList();
            }
            Map<String, Object> beforeMap = JsonUtil.fromJson(before, Map.class);
            Map<String, Object> afterMap = JsonUtil.fromJson(after, Map.class);
            if (beforeMap == null || afterMap == null) {
                return Collections.<Map<String, Object>>emptyList();
            }
            List<Map<String, Object>> diffs = new ArrayList<>();
            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(beforeMap.keySet());
            allKeys.addAll(afterMap.keySet());
            for (String key : allKeys) {
                Object oldVal = beforeMap.get(key);
                Object newVal = afterMap.get(key);
                if (!Objects.equals(oldVal, newVal)) {
                    Map<String, Object> diff = new LinkedHashMap<>();
                    diff.put("field", key);
                    diff.put("oldValue", oldVal);
                    diff.put("newValue", newVal);
                    diffs.add(diff);
                }
            }
            return diffs;
        }).orElse(Collections.emptyList());
    }
}
