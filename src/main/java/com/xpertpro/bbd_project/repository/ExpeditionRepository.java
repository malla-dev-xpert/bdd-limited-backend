package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Expeditions;
import com.xpertpro.bbd_project.entity.Harbor;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpeditionRepository extends JpaRepository<Expeditions, Long> {
    Page<Expeditions> findByStatus(StatusEnum status, Pageable pageable);
    @Query("SELECT e FROM Expeditions e WHERE " +
            "e.status = :status AND " +
            "LOWER(e.ref) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Expeditions> findByStatusAndSearchQuery(
            @Param("status") StatusEnum status,
            @Param("query") String query,
            Pageable pageable
    );
}
