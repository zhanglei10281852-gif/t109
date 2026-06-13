package com.company.material.controller;

import com.company.material.entity.AccountLock;
import com.company.material.entity.LoginLog;
import com.company.material.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/login-logs")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    private boolean notAdmin(String role) {
        return !"管理员".equals(role);
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestAttribute("role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可访问"));
        }
        PageRequest pr = PageRequest.of(page, size, Sort.by("loginTime").descending());
        Page<LoginLog> resultPage = loginLogService.search(username, result, startTime, endTime, pr);
        return ResponseEntity.ok(resultPage);
    }

    @GetMapping("/locked-accounts")
    public ResponseEntity<?> getLockedAccounts(@RequestAttribute("role") String role) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可访问"));
        }
        List<Map<String, Object>> lockedAccounts = loginLogService.getLockedAccounts().stream()
                .map(lock -> Map.<String, Object>of(
                        "userId", lock.getUserId(),
                        "username", lock.getUsername(),
                        "lockStartTime", lock.getLockStartTime(),
                        "lockEndTime", lock.getLockEndTime(),
                        "reason", lock.getReason(),
                        "failedCount", lock.getFailedCount(),
                        "remainingMinutes", Math.max(0,
                                java.time.Duration.between(LocalDateTime.now(), lock.getLockEndTime()).toMinutes())
                )).toList();
        return ResponseEntity.ok(Map.of(
                "total", lockedAccounts.size(),
                "accounts", lockedAccounts
        ));
    }

    @GetMapping("/users/{userId}/lock-history")
    public ResponseEntity<?> getLockHistory(@RequestAttribute("role") String role, @PathVariable Long userId) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可访问"));
        }
        List<AccountLock> history = loginLogService.getAccountLockHistory(userId);
        return ResponseEntity.ok(history);
    }
}
