package com.company.material.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "login_logs", indexes = {
    @Index(name = "idx_login_user", columnList = "userId"),
    @Index(name = "idx_login_time", columnList = "loginTime"),
    @Index(name = "idx_login_result", columnList = "result"),
    @Index(name = "idx_login_ip", columnList = "loginIp")
})
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private LocalDateTime loginTime;

    @Column(nullable = false, length = 50)
    private String loginIp;

    @Column(nullable = false, length = 10)
    private String result;

    @Column(length = 200)
    private String failureReason;

    @PrePersist
    public void prePersist() {
        if (this.loginTime == null) {
            this.loginTime = LocalDateTime.now();
        }
    }
}
