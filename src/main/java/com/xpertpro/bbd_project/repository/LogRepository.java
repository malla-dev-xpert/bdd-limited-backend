package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.Logs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Logs, Long> {
}
