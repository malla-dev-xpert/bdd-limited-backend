package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Containers;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContainersRepository extends JpaRepository<Containers, Long> {
    Optional<Containers> findByReference(String reference);
    Page<Containers> findByStatus(StatusEnum status, Pageable pageable);
}
