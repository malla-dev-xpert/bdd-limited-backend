package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.achats.CreateAchatDto;
import com.xpertpro.bbd_project.services.AchatServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/achats")
@CrossOrigin("*")
public class AchatController {
    @Autowired
    AchatServices achatServices;

    @PostMapping("/create")
    public ResponseEntity<String> createAchat(
            @RequestParam Long clientId,
            @RequestParam Long supplierId,
            @RequestParam Long userId,
            @RequestBody CreateAchatDto dto) {
        try {
            System.out.println("Creating achat for client: {}, supplier: {}, user: {}"+ clientId + supplierId + userId);
            System.out.println("DTO received: {} "+ dto);

            String result = achatServices.createAchatForClient(clientId, supplierId, userId, dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.out.println("Error creating achat"+ e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("SERVER_ERROR: " + e.getMessage());
        }
    }
}