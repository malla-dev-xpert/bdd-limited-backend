package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Items;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemsRepository extends JpaRepository<Items, Long> {
    List<Items> findByPackagesId(Long packageId);
    Page<Items> findByStatusNot(StatusEnum status, Pageable pageable);
}
