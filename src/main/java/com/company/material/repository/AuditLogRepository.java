package com.company.material.repository;

import com.company.material.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByOperatorId(Long operatorId, Pageable pageable);

    Page<AuditLog> findByBusinessModule(String businessModule, Pageable pageable);

    Page<AuditLog> findByOperationType(String operationType, Pageable pageable);

    Page<AuditLog> findByResult(String result, Pageable pageable);

    Page<AuditLog> findByOperationTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:operatorId IS NULL OR a.operatorId = :operatorId) AND " +
           "(:businessModule IS NULL OR a.businessModule = :businessModule) AND " +
           "(:operationType IS NULL OR a.operationType = :operationType) AND " +
           "(:result IS NULL OR a.result = :result) AND " +
           "(:startTime IS NULL OR a.operationTime >= :startTime) AND " +
           "(:endTime IS NULL OR a.operationTime <= :endTime) " +
           "ORDER BY a.operationTime DESC")
    Page<AuditLog> findByConditions(
            @Param("operatorId") Long operatorId,
            @Param("businessModule") String businessModule,
            @Param("operationType") String operationType,
            @Param("result") String result,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    @Query("SELECT a.businessModule, COUNT(a) FROM AuditLog a WHERE a.operationTime >= :startTime GROUP BY a.businessModule")
    List<Object[]> countByModule(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT a.operationType, COUNT(a) FROM AuditLog a WHERE a.operationTime >= :startTime GROUP BY a.operationType")
    List<Object[]> countByOperationType(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT a.operatorId, a.operatorName, COUNT(a) FROM AuditLog a WHERE a.operationTime >= :startTime GROUP BY a.operatorId, a.operatorName ORDER BY COUNT(a) DESC")
    List<Object[]> countByOperator(@Param("startTime") LocalDateTime startTime, Pageable pageable);

    @Query("SELECT a.operatorId, a.operatorName, COUNT(a) FROM AuditLog a " +
           "WHERE a.operationType = '删除' AND a.operationTime >= :startTime " +
           "GROUP BY a.operatorId, a.operatorName HAVING COUNT(a) >= :threshold ORDER BY COUNT(a) DESC")
    List<Object[]> findMassDeleteOperations(
            @Param("startTime") LocalDateTime startTime,
            @Param("threshold") int threshold);

    long countByOperationTimeBetweenAndResult(LocalDateTime start, LocalDateTime end, String result);
}
