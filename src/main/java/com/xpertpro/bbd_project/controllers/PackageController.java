package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.PackageDto;
import com.xpertpro.bbd_project.services.ContainerPackageService;
import com.xpertpro.bbd_project.services.PackageServices;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/packages")
@CrossOrigin("*")
public class PackageController {
    @Autowired
    PackageServices packageServices;
    @Autowired
    ContainerPackageService containerPackageService;

    @PostMapping("/create")
    public ResponseEntity<String> newExpedition(
            @RequestBody PackageDto dto,
            @RequestParam(name = "clientId") Long clientId,
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "containerId", required = false) Long containerId,
            @RequestParam(name = "warehouseId") Long warehouseId)
    {
        try{
            packageServices.create(dto, clientId, userId, containerId, warehouseId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Colis ajouter avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping()
    public List<PackageDto> findAll(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) String query) {
        return packageServices.getAll(page, query);
    }

    @GetMapping("/received")
    public List<PackageDto> findAllInPending(@RequestParam(defaultValue = "0") int page) {
        return packageServices.getAllEnAttente(page);
    }

    @DeleteMapping("/{packageId}/container/{containerId}")
    public ResponseEntity<String> removePackageFromContainer(
            @PathVariable Long packageId,
            @PathVariable Long containerId,
            @RequestParam Long userId) {

        try {
            String result = containerPackageService.retirerColis(packageId, containerId, userId);

            return ResponseEntity.ok()
                    .body(("SUCCESS"+ result));

        } catch (ContainerPackageService.ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("ERROR"+ ex.getMessage()));

        } catch (ContainerPackageService.OperationNotAllowedException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("ERROR"+ ex.getMessage()));

        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(("ERROR"+ "Erreur interne du serveur"));
        }
    }

    @DeleteMapping("/start-expedition")
    public ResponseEntity<String> startExpedition(@RequestParam(name = "expeditionId") Long expeditionId) {
        try {
            packageServices.startExpedition(expeditionId);
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
            packageServices.confirmExpedition(expeditionId);
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
            packageServices.receivedExpedition(expeditionId);
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
            packageServices.deleteExpedition(expeditionId);
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
            @RequestBody PackageDto packageDto,
            @RequestParam("userId") Long userId) {

        try {
            String result = packageServices.updateExpedition(id, packageDto, userId);

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

    @GetMapping("/warehouse")
    public List<PackageDto> getPackageByWarehouse(@RequestParam(name = "warehouseId") Long warehouseId) {
        return packageServices.getPackagesByWarehouse(warehouseId);
    }

}
