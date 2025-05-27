package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.entity.Logs;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.repository.LogRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogServices {

    private final LogRepository logRepository;

    @Transactional()
    public void logAction(
            UserEntity user,
            String action,
            String entityType,
            Long entityId
    ) {
        Logs log = new Logs();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);

        logRepository.save(log);
    }
}
