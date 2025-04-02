package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Containers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContainersRepository extends JpaRepository<Containers, Long> {
    Optional<Containers> findByReference(String reference);
}
