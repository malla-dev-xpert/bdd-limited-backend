package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Versements;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VersementRepo extends JpaRepository<Versements, Long> {
    List<Versements> findByPartnerId(Long partnerId);
    Page<Versements> findByStatusNot(StatusEnum status, Pageable pageable);
    @Query(value = "SELECT v.montantRestant FROM Versements v WHERE v.partner.id = :partnerId AND v.status = 'CREATE' ORDER BY v.createdAt DESC LIMIT 1", nativeQuery = true)
    Optional<Double> findLastMontantRestantByPartnerId(Long partnerId);

    @Query(value = "SELECT SUM(v.montantRestant) FROM Versements v WHERE v.partner.id = :partnerId AND v.status = 'CREATE'", nativeQuery = true)
    Double getTotalMontantRestantByPartnerId(Long partnerId);

    Optional<Versements> findFirstByPartnerIdAndStatusOrderByCreatedAtDesc(Long partnerId, StatusEnum status);

}
