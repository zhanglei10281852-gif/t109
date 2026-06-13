package com.company.material.service;

import com.company.material.entity.AccountLock;
import com.company.material.repository.AccountLockRepository;
import com.company.material.repository.AuditLogRepository;
import com.company.material.repository.LoginLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final AuditLogRepository auditLogRepository;
    private final LoginLogRepository loginLogRepository;
    private final AccountLockRepository accountLockRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getOperationStats(int days) {
        Map<String, Object> result = new LinkedHashMap<>();
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        List<Object[]> byModule = auditLogRepository.countByModule(startTime);
        Map<String, Long> moduleStats = new LinkedHashMap<>();
        for (Object[] row : byModule) {
            moduleStats.put((String) row[0], (Long) row[1]);
        }
        result.put("byModule", moduleStats);

        List<Object[]> byType = auditLogRepository.countByOperationType(startTime);
        Map<String, Long> typeStats = new LinkedHashMap<>();
        for (Object[] row : byType) {
            typeStats.put((String) row[0], (Long) row[1]);
        }
        result.put("byOperationType", typeStats);

        long total = auditLogRepository.countByOperationTimeBetweenAndResult(startTime, LocalDateTime.now(), "成功") +
                     auditLogRepository.countByOperationTimeBetweenAndResult(startTime, LocalDateTime.now(), "失败");
        long success = auditLogRepository.countByOperationTimeBetweenAndResult(startTime, LocalDateTime.now(), "成功");
        result.put("totalOperations", total);
        result.put("successRate", total > 0 ? (double) success / total * 100 : 100.0);

        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLoginStats(int days) {
        Map<String, Object> result = new LinkedHashMap<>();
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        long total = loginLogRepository.countByLoginTimeBetweenAndResult(startTime, LocalDateTime.now(), "成功") +
                     loginLogRepository.countByLoginTimeBetweenAndResult(startTime, LocalDateTime.now(), "失败");
        long success = loginLogRepository.countByLoginTimeBetweenAndResult(startTime, LocalDateTime.now(), "成功");

        result.put("totalLogins", total);
        result.put("successLogins", success);
        result.put("failedLogins", total - success);
        result.put("successRate", total > 0 ? (double) success / total * 100 : 100.0);

        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopActiveUsers(int days, int limit) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        List<Object[]> users = auditLogRepository.countByOperator(startTime, PageRequest.of(0, limit));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : users) {
            Map<String, Object> user = new LinkedHashMap<>();
            user.put("userId", row[0]);
            user.put("userName", row[1]);
            user.put("operationCount", row[2]);
            result.add(user);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> detectAbnormalOperations(int minutes, int threshold) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(minutes);
        List<Object[]> massDeletes = auditLogRepository.findMassDeleteOperations(startTime, threshold);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : massDeletes) {
            Map<String, Object> op = new LinkedHashMap<>();
            op.put("userId", row[0]);
            op.put("userName", row[1]);
            op.put("deleteCount", row[2]);
            op.put("timeWindow", minutes + "分钟内");
            op.put("risk", "高频删除操作");
            result.add(op);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLockedAccounts() {
        List<AccountLock> locks = accountLockRepository.findAllActiveLocks(LocalDateTime.now());
        List<Map<String, Object>> result = new ArrayList<>();
        for (AccountLock lock : locks) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("userId", lock.getUserId());
            item.put("username", lock.getUsername());
            item.put("lockStartTime", lock.getLockStartTime());
            item.put("lockEndTime", lock.getLockEndTime());
            item.put("reason", lock.getReason());
            item.put("failedCount", lock.getFailedCount());
            long remainingMinutes = java.time.Duration.between(LocalDateTime.now(), lock.getLockEndTime()).toMinutes();
            item.put("remainingMinutes", Math.max(0, remainingMinutes));
            result.add(item);
        }
        return result;
    }
}
