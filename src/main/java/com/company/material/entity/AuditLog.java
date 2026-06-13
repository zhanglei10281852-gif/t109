package com.company.material.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_operator", columnList = "operatorId"),
    @Index(name = "idx_module", columnList = "businessModule"),
    @Index(name = "idx_operation_type", columnList = "operationType"),
    @Index(name = "idx_operation_time", columnList = "operationTime"),
    @Index(name = "idx_result", columnList = "result")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long operatorId;

    @Column(nullable = false, length = 50)
    private String operatorName;

    @Column(nullable = false, length = 20)
    private String operatorRole;

    @Column(nullable = false, length = 20)
    private String operationType;

    @Column(nullable = false, length = 30)
    private String businessModule;

    private Long targetId;

    @Column(length = 200)
    private String targetName;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String beforeSnapshot;

    @Column(columnDefinition = "TEXT")
    private String afterSnapshot;

    @Column(length = 50)
    private String requestIp;

    @Column(nullable = false)
    private LocalDateTime operationTime;

    @Column(nullable = false, length = 10)
    private String result;

    @Column(length = 500)
    private String errorMessage;

    @PrePersist
    public void prePersist() {
        if (this.operationTime == null) {
            this.operationTime = LocalDateTime.now();
        }
    }
}
