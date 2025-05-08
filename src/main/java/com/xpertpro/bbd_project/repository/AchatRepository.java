package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Achats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AchatRepository extends JpaRepository<Achats, Long> {
}
