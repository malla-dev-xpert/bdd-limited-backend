package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Roles;
import com.xpertpro.bbd_project.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Roles, Long> {
    Optional<Roles> findByName(RoleEnum roleName);
}
