package com.company.material.controller;

import com.company.material.entity.AuditLog;
import com.company.material.service.AuditLogService;
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
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    private boolean notAdmin(String role) {
        return !"管理员".equals(role);
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestAttribute("role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String businessModule,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可访问"));
        }
        PageRequest pr = PageRequest.of(page, size, Sort.by("operationTime").descending());
        Page<AuditLog> resultPage = auditLogService.search(operatorId, businessModule, operationType,
                result, startTime, endTime, pr);
        return ResponseEntity.ok(resultPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@RequestAttribute("role") String role, @PathVariable Long id) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可访问"));
        }
        return auditLogService.findById(id)
                .map(log -> ResponseEntity.ok((Object) log))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/diff")
    public ResponseEntity<?> getDiff(@RequestAttribute("role") String role, @PathVariable Long id) {
        if (notAdmin(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "无权限，仅管理员可访问"));
        }
        List<Map<String, Object>> diffs = auditLogService.compareSnapshots(id);
        return ResponseEntity.ok(Map.of(
                "auditLogId", id,
                "changes", diffs,
                "changeCount", diffs.size()
        ));
    }
}
