package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.logs.SessionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionLogRepository extends JpaRepository<SessionLog, Long> {

    public SessionLog findByUsernameAndJwtTokenAndLogoutTimeIsNull(String username, String token);
    List<SessionLog> findByUsernameAndLogoutTimeIsNull(String username);
}
