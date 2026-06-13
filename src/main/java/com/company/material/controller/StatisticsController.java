package com.company.material.controller;

import com.company.material.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    private boolean notAdmin(String role) {
        return !"管理员".equals(role);
    }

    @GetMapping("/operations")
    public ResponseEntity<?> getOperationStats(
            @RequestAttribute("role") String role,
            @RequestParam(defaultValue = "7") int days) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可访问"));
        }
        return ResponseEntity.ok(statisticsService.getOperationStats(days));
    }

    @GetMapping("/logins")
    public ResponseEntity<?> getLoginStats(
            @RequestAttribute("role") String role,
            @RequestParam(defaultValue = "7") int days) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可访问"));
        }
        return ResponseEntity.ok(statisticsService.getLoginStats(days));
    }

    @GetMapping("/active-users")
    public ResponseEntity<?> getTopActiveUsers(
            @RequestAttribute("role") String role,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可访问"));
        }
        return ResponseEntity.ok(Map.of(
                "days", days,
                "limit", limit,
                "users", statisticsService.getTopActiveUsers(days, limit)
        ));
    }

    @GetMapping("/abnormal-operations")
    public ResponseEntity<?> detectAbnormalOperations(
            @RequestAttribute("role") String role,
            @RequestParam(defaultValue = "10") int minutes,
            @RequestParam(defaultValue = "10") int threshold) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可访问"));
        }
        return ResponseEntity.ok(Map.of(
                "timeWindow", minutes + "分钟",
                "threshold", threshold + "次",
                "abnormalOperations", statisticsService.detectAbnormalOperations(minutes, threshold)
        ));
    }

    @GetMapping("/locked-accounts")
    public ResponseEntity<?> getLockedAccounts(@RequestAttribute("role") String role) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可访问"));
        }
        return ResponseEntity.ok(Map.of(
                "total", statisticsService.getLockedAccounts().size(),
                "accounts", statisticsService.getLockedAccounts()
        ));
    }
}
