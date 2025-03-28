package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Harbor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HarborRepository extends JpaRepository<Harbor, Long> {
    Optional<Harbor> findByName(String name);
}
