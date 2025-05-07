package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Versements;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VersementRepo extends JpaRepository<Versements, Long> {
    List<Versements> findByPartnerId(Long partnerId);
    Page<Versements> findByStatusNot(StatusEnum status, Pageable pageable);
}
