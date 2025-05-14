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
            @RequestParam(name = "clientId")Long clientId,
            @RequestParam(name = "supplierId")Long supplierId,
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "warehouseId") Long warehouseId,
            @RequestParam(name = "containerId") Long containerId,
            @RequestBody CreateAchatDto dto
    ) {
        String result = achatServices.createAchatForClient(clientId,supplierId, userId, warehouseId, containerId, dto);
        switch (result){
            case "INVALID_VERSEMENT":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Invalid versement.");
            case "INACTIF_VERSEMENT":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Inactif versement.");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("ACHAT_CREATED");
        }
    }
}
