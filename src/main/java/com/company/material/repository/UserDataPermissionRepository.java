package com.company.material.repository;

import com.company.material.entity.UserDataPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserDataPermissionRepository extends JpaRepository<UserDataPermission, Long> {

    Optional<UserDataPermission> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    void deleteByUserId(Long userId);
}
