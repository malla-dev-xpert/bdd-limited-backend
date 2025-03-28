package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Harbor;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HarborRepository extends JpaRepository<Harbor, Long> {
    Optional<Harbor> findByName(String name);
    Page<Harbor> findByStatus(StatusEnum status, Pageable pageable);
}
