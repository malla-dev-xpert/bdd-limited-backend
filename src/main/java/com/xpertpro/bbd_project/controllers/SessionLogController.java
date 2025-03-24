package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.logs.SessionLog;
import com.xpertpro.bbd_project.repository.SessionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/users/logs")
@CrossOrigin("*")
public class SessionLogController {

    @Autowired
    SessionLogRepository sessionLogRepository;

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionLog>> getAllSessions() {
        List<SessionLog> sessions = sessionLogRepository.findAll();
        return ResponseEntity.ok(sessions);
    }
}
