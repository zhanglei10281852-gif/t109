package com.company.material.repository;

import com.company.material.entity.AccountLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AccountLockRepository extends JpaRepository<AccountLock, Long> {

    @Query("SELECT al FROM AccountLock al WHERE al.userId = :userId AND al.unlocked = false AND al.lockEndTime > :now ORDER BY al.lockStartTime DESC")
    Optional<AccountLock> findActiveLock(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT al FROM AccountLock al WHERE al.unlocked = false AND al.lockEndTime > :now ORDER BY al.lockStartTime DESC")
    List<AccountLock> findAllActiveLocks(@Param("now") LocalDateTime now);

    List<AccountLock> findByUserIdOrderByLockStartTimeDesc(Long userId);
}
