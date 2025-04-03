package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Countries;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CountriesRepository extends JpaRepository<Countries, Long> {
    Optional<Countries> findByName(String name);
    Optional<Countries> findByIsoCode(String isoCode);
    Page<Countries> findByStatus(StatusEnum status, Pageable pageable);
}
