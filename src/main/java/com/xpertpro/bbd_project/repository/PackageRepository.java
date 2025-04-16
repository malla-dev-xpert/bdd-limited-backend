package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Packages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Packages, Long> {
    List<Packages> findByWarehouseIdAndContainerIsNull(Long warehouseId);
    Optional<Packages> findByReference(String reference);
    List<Packages> findByWarehouseId(Long warehouseId);
}
