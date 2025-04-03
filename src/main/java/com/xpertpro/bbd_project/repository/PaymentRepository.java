package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Payments;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payments, Long> {
    Page<Payments> findByStatus(StatusEnum status, Pageable pageable);
}
