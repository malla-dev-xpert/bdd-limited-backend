package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.logs.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {
}
