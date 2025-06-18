package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Achats;
import com.xpertpro.bbd_project.entity.Packages;
import com.xpertpro.bbd_project.enums.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchatRepository extends JpaRepository<Achats, Long> {
    boolean existsByVersementId(Long versementId);
    Page<Achats> findByStatusNot(StatusEnum statusEnum, Pageable pageable);
}
