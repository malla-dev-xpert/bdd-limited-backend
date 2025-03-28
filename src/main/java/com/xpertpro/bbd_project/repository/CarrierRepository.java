package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Carriers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarrierRepository extends JpaRepository<Carriers, Long> {
    Optional<Carriers> findByContact(String contact);
}
