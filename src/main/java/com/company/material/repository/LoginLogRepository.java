package com.company.material.repository;

import com.company.material.entity.LoginLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    Page<LoginLog> findByUsername(String username, Pageable pageable);

    Page<LoginLog> findByResult(String result, Pageable pageable);

    Page<LoginLog> findByLoginTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT l FROM LoginLog l WHERE " +
           "(:username IS NULL OR l.username = :username) AND " +
           "(:result IS NULL OR l.result = :result) AND " +
           "(:startTime IS NULL OR l.loginTime >= :startTime) AND " +
           "(:endTime IS NULL OR l.loginTime <= :endTime) " +
           "ORDER BY l.loginTime DESC")
    Page<LoginLog> findByConditions(
            @Param("username") String username,
            @Param("result") String result,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    @Query("SELECT COUNT(l) FROM LoginLog l WHERE l.userId = :userId AND l.result = '失败' AND l.loginTime >= :since")
    long countFailedLoginsSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    @Query("SELECT l FROM LoginLog l WHERE l.userId = :userId AND l.result = '失败' AND l.loginTime >= :since ORDER BY l.loginTime DESC")
    List<LoginLog> findRecentFailedLogins(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    long countByLoginTimeBetweenAndResult(LocalDateTime start, LocalDateTime end, String result);
}
