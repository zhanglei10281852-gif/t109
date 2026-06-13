package com.company.material.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "suppliers")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String supplierCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String contactPerson;

    @Column(length = 20)
    private String phone;

    @Column(length = 200)
    private String address;

    @Column(length = 50)
    private String category;

    @Column(nullable = false, length = 10)
    private String status;

    private Long createdBy;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = "合作中";
        if (this.createdBy == null) {
            this.createdBy = com.company.material.util.HttpContextUtil.getCurrentUserId();
        }
    }
}
