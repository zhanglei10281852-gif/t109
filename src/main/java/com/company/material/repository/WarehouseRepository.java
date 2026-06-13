package com.company.material.repository;

import com.company.material.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByWarehouseCode(String warehouseCode);
    boolean existsByWarehouseCode(String warehouseCode);
    List<Warehouse> findByStatus(String status);
    List<Warehouse> findByCreatedBy(Long createdBy);

    @Query("SELECT w FROM Warehouse w WHERE w.id IN :ids")
    List<Warehouse> findByIds(@Param("ids") List<Long> ids);
}
