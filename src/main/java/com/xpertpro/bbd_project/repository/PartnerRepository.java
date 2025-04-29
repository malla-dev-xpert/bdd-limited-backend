package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Partners;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partners, Long> {
    Page<Partners> findByAccountType(String accountType, Pageable pageable);
    Optional<Partners> findByEmail(String email);
    Optional<Partners> findByPhoneNumber(String phoneNumber);
    Page<Partners> findByStatus(StatusEnum status, Pageable pageable);
    @Query("SELECT h FROM Harbor h WHERE " +
            "h.status = :status AND " +
            "(LOWER(h.name) LIKE LOWER(:query) OR " +
            "LOWER(h.location) LIKE LOWER(:query))")
    Page<Partners> findByStatusAndSearchQuery(
            @Param("status") StatusEnum status,
            @Param("query") String query,
            Pageable pageable
    );
}
