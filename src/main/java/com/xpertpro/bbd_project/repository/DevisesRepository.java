package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Devises;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DevisesRepository extends JpaRepository<Devises, Long> {
    Optional<Devises> findByCode(String code);
    Page<Devises> findByStatus(StatusEnum status, Pageable pageable);
}
