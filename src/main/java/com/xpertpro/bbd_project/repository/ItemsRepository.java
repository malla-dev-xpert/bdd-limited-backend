package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Items;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemsRepository extends JpaRepository<Items, Long> {
    List<Items> findByPackagesId(Long packageId);
}
