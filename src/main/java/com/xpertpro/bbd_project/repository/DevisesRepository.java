package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Devises;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DevisesRepository extends JpaRepository<Devises, Long> {
    Optional<Devises> findByCode(String code);
}
