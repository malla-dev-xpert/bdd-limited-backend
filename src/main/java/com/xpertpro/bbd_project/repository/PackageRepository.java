package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Packages;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Packages, Long> {
    Page<Packages> findByStatusNot(StatusEnum status, Pageable pageable);
    @Query("SELECT p FROM Packages p WHERE " +
            "p.status = :status AND " +
            "LOWER(p.ref) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Packages> findByStatusAndSearchQuery(
            @Param("status") StatusEnum status,
            @Param("query") String query,
            Pageable pageable
    );
    Optional<Packages> findByRef(String ref);
    boolean existsByClientId(Long partnerId);
    boolean existsByWarehouseId(Long id);
    List<Packages> findByWarehouseId(Long warehouseId);
    Page<Packages> findByStatusAndContainerIsNull(StatusEnum status, Pageable pageable);
}
