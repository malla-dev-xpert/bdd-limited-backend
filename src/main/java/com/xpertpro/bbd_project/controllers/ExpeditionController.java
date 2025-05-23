package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.ExpeditionDto;
import com.xpertpro.bbd_project.services.ExpeditionServices;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping()
    public List<ExpeditionDto> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) String query) {
        return expeditionServices.getAll(page, query);
    }

    @DeleteMapping("/start-expedition")
    public ResponseEntity<String> startExpedition(@RequestParam(name = "expeditionId") Long expeditionId) {
        try {
            expeditionServices.startExpedition(expeditionId);
            return ResponseEntity.status(HttpStatus.OK).body("L'expédition a démarré avec succès.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur : " + e.getMessage());
        }
    }

    @DeleteMapping("/deliver-expedition")
    public ResponseEntity<String> confirmExpedition(@RequestParam(name = "expeditionId") Long expeditionId) {
        try {
            expeditionServices.confirmExpedition(expeditionId);
            return ResponseEntity.status(HttpStatus.OK).body("L'expédition est arrivé avec succès.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur : " + e.getMessage());
        }
    }

    @DeleteMapping("/received-expedition")
    public ResponseEntity<String> receivedExpedition(@RequestParam(name = "expeditionId") Long expeditionId) {
        try {
            expeditionServices.receivedExpedition(expeditionId);
            return ResponseEntity.status(HttpStatus.OK).body("L'expédition est livrée avec succès.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur : " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteExpedition(@RequestParam(name = "expeditionId") Long expeditionId) {
        try {
            expeditionServices.deleteExpedition(expeditionId);
            return ResponseEntity.status(HttpStatus.OK).body("L'expédition a été supprimé avec succès.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur : " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateExpedition(
            @PathVariable Long id,
            @RequestBody ExpeditionDto expeditionDto,
            @RequestParam("userId") Long userId) {

        try {
            String result = expeditionServices.updateExpedition(id, expeditionDto, userId);

            switch (result) {
                case "SUCCESS":
                    return ResponseEntity.ok().body("Expedition updated successfully");
                case "CLIENT_NOT_FOUND":
                    return ResponseEntity.badRequest().body("Client not found with provided ID");
                default:
                    return ResponseEntity.internalServerError().body("Unexpected error occurred");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred while updating the expedition");
        }
    }

}
