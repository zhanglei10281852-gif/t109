package com.company.material.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "account_locks", indexes = {
    @Index(name = "idx_lock_user", columnList = "userId"),
    @Index(name = "idx_lock_time", columnList = "lockEndTime"),
    @Index(name = "idx_unlocked", columnList = "unlocked")
})
public class AccountLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private LocalDateTime lockStartTime;

    @Column(nullable = false)
    private LocalDateTime lockEndTime;

    @Column(nullable = false, length = 100)
    private String reason;

    @Column(nullable = false)
    private Integer failedCount;

    @Column(nullable = false)
    private Boolean unlocked = false;

    private LocalDateTime unlockTime;

    @PrePersist
    public void prePersist() {
        if (this.lockStartTime == null) {
            this.lockStartTime = LocalDateTime.now();
        }
    }
}
