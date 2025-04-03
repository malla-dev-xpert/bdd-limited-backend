package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Category;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    Page<Category> findByStatus(StatusEnum status, Pageable pageable);
}
