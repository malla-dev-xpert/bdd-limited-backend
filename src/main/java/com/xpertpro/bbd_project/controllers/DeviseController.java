package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.devises.DeviseDto;
import com.xpertpro.bbd_project.entity.Devises;
import com.xpertpro.bbd_project.services.DeviseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/devises")
@CrossOrigin("*")
public class DeviseController {

    @Autowired
    DeviseService deviseService;

    @PostMapping("/create")
    public ResponseEntity<String> createDevise(@RequestBody DeviseDto deviseDto, @RequestParam Long userId){
        String result = deviseService.createDevise(deviseDto, userId);
        switch (result) {
            case "NAME_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Nom de devise déjà utilisé !");
            case "CODE_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Code déjà utilisé !");
            case "RATE_NOT_FOUND":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Le taux de conversion depuis le Yuan Chinois (CNY) n'a pas pu être récupéré automatiquement.");
            case "RATE_SERVICE_ERROR":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Le service de taux de change est temporairement indisponible.");
            case "GENERAL_ERROR":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Une erreur inattendue s'est produite. Veuillez réessayer ou contacter le support.");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Devise ajouté avec succès !");
        }
    }

    @PutMapping("/update/{id}")
    public DeviseDto updateDevises(@PathVariable Long id, @RequestBody DeviseDto deviseDto) {
        return deviseService.updateDevise(id, deviseDto);
    }

    @GetMapping()
    public Page<Devises> getAllDevises(@RequestParam(defaultValue = "0") int page) {
        return deviseService.getAllDevises(page);
    }

    @GetMapping("/{id}")
    public Devises getDevisesById(@PathVariable Long id) {
        return deviseService.getDeviseById(id);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteDevises(@PathVariable Long id){
        deviseService.deleteDevises(id);
        return "Le devise avec a été supprimer avec succès.";
    }
}
