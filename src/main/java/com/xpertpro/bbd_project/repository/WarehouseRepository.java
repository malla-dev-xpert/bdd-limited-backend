package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Warehouse;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByName(String name);
    Optional<Warehouse> findByAdresse(String adresse);
    Page<Warehouse> findByStatus(StatusEnum status, Pageable pageable);
}
