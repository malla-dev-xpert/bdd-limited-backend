package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.LigneAchat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LigneAchatRepository extends JpaRepository<LigneAchat, Long> {
}
