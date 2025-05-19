package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.ExpeditionDto;
import com.xpertpro.bbd_project.services.ExpeditionServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/expeditions")
@CrossOrigin("*")
public class ExpeditionController {
    @Autowired
    ExpeditionServices expeditionServices;

    @PostMapping("/create")
    public ResponseEntity<String> newExpedition(
            @RequestBody ExpeditionDto dto,
            @RequestParam(name = "clientId") Long clientId,
            @RequestParam(name = "userId") Long userId)
    {
        try{
            expeditionServices.create(dto, clientId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Expedition ajouter avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}
