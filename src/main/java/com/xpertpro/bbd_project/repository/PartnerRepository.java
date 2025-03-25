package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Partners;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partners, Long> {
    Page<Partners> findByAccountType(String accountType, Pageable pageable);
    Optional<Partners> findByEmail(String email);
    Optional<Partners> findByPhoneNumber(String phoneNumber);
    Page<Partners> findByStatus(StatusEnum status, Pageable pageable);
}
