package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.containers.ContainersDto;
import com.xpertpro.bbd_project.entity.Containers;
import com.xpertpro.bbd_project.entity.EmbarquementRequest;
import com.xpertpro.bbd_project.repository.ContainersRepository;
import com.xpertpro.bbd_project.services.ContainerPackageService;
import com.xpertpro.bbd_project.services.ContainerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("api/v1/containers")
@CrossOrigin("*")
public class ContainersController {

    @Autowired
    ContainerServices containerServices;
    @Autowired
    ContainersRepository containersRepository;
    @Autowired
    ContainerPackageService containerPackageService;

    @PostMapping("/create")
    public ResponseEntity<String> addContainers(@RequestBody ContainersDto containersDto, @RequestParam(name = "userId") Long userId) {
        String result = containerServices.createContainer(containersDto, userId);
        switch (result) {
            case "REF_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Numéro d'identification déjà utilisé !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Le conteneur a été ajouté avec succès!");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateContainer
            (@PathVariable Long id,
             @RequestParam(name = "userId") Long userId,
             @RequestBody ContainersDto containersDto)
    {
        String result = containerServices.updateContainer(id,containersDto, userId);
        switch (result) {
            case "REF_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce conteneur existe déjà !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Conteneur modifier avec succès !");
        }
    }

    @PostMapping("/embarquer")
    public ResponseEntity<Containers> embarquerColis(
            @RequestBody EmbarquementRequest request) {
        return ResponseEntity.ok(
                containerPackageService.embarquerColis(request));
    }

    @GetMapping()
    public List<ContainersDto> getAllContainers(@RequestParam(defaultValue = "0") int page) {
        return containerServices.getAllContainers(page);
    }

    @GetMapping("/{id}")
    public ContainersDto getContainerById(@PathVariable Long id){
        return containerServices.getContainerById(id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteContainer(@PathVariable Long id, @RequestParam(name = "userId") Long userId){
        String result = containerServices.deleteContainerById(id, userId);
        switch (result) {
            case "CONTAINER_NOT_FOUND":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Conteneur introuvable.");
            case "USER_NOT_FOUND":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Utilisateur introuvable.");
            case "PACKAGE_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Impossible de supprimer : Des colis existent dans ce conteneur.");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Le conteneur a été supprimer avec succès!");
        }
    }

    @DeleteMapping("/retrieve/{id}/harbor/{harborId}")
    public String retrieveContainerToHarbor(@PathVariable Long id, @RequestParam(name = "userId") Long userId, @PathVariable("harborId") Long harborId){
        containerServices.retrieveContainerToHArbor(id, userId, harborId);
        return "Le Conteneur avec  l'id " + id + " a été retirer avec succès.";
    }
}
