package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Partners;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartnerRepository extends JpaRepository<Partners, Long> {
    List<Partners> findByAccountType(String typeCompte);
    Optional<Partners> findByEmail(String email);
    Optional<Partners> findByPhoneNumber(String phoneNumber);
}
