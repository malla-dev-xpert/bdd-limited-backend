package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.RolesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RolesEntity, Long> {

    Optional<RolesEntity> findByName(String name);
}
