package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.containers.ContainersDto;
import com.xpertpro.bbd_project.entityMapper.EmbarquementRequest;
import com.xpertpro.bbd_project.entityMapper.HarborEmbarquementRequest;
import com.xpertpro.bbd_project.repository.ContainersRepository;
import com.xpertpro.bbd_project.services.ContainerPackageService;
import com.xpertpro.bbd_project.services.ContainerServices;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


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
    public ResponseEntity<String> addContainers(
            @RequestBody ContainersDto containersDto,
            @RequestParam(name = "userId") Long userId,
            @RequestParam(required = false) Long supplierId) {

        try {
            String result = containerServices.createContainer(containersDto, userId, supplierId);

            return switch (result) {
                case "REF_EXIST" -> ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Numéro d'identification déjà utilisé !");
                case "SUCCESS" -> ResponseEntity.status(HttpStatus.CREATED)
                        .body("Le conteneur a été ajouté avec succès!");
                default -> ResponseEntity.internalServerError()
                        .body("Une erreur inattendue est survenue");
            };
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erreur lors de la création du conteneur: " + e.getMessage());
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
    public ResponseEntity<String> embarquerColis(
            @RequestBody EmbarquementRequest request,
            @RequestParam(name = "userId") Long userId) {

        try{
            String result = containerPackageService.embarquerColis(request, userId);

            switch (result) {
                case "CONTAINER_NOT_AVAILABLE":
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Le conteneur n'est pas disponible pour l'embarquement.");
                case "CONTAINER_NOT_IN_PENDING":
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Le conteneur n'est pas dans le bon statut pour l'embarquement.");
                default:
                    return ResponseEntity.status(HttpStatus.CREATED).body("Colis embarquer avec succès !");
            }
        }catch (ContainerPackageService.OperationNotAllowedException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }

    }

    @PostMapping("/embarquer/in-harbor")
    public ResponseEntity<String> embarquerContainerInHarbor(
            @RequestBody HarborEmbarquementRequest request,
            @RequestParam(name = "userId") Long userId) {

        try{
            String result = containerPackageService.embarquerConteneursDansPort(request, userId);

            switch (result) {
                case "HARBOR_NOT_AVAILABLE":
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Le port n'est pas disponible pour l'embarquement.");
                default:
                    return ResponseEntity.status(HttpStatus.CREATED).body("Conteneurs embarquer avec succès !");
            }
        }catch (ContainerPackageService.OperationNotAllowedException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }

    }

    @GetMapping()
    public List<ContainersDto> getAllContainers(@RequestParam(defaultValue = "0") int page) {
        return containerServices.getAllContainers(page);
    }

    @GetMapping("/not-in-harbor")
    public List<ContainersDto> getAllContainersNotInHarbor(@RequestParam(defaultValue = "0") int page) {
        return containerServices.getAllContainersNotInHarbor(page);
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

    @DeleteMapping("/retrieve/harbor")
    public ResponseEntity<String> retrieveContainerToHarbor(@RequestParam(name = "containerId") Long containerId, @RequestParam(name = "userId") Long userId, @RequestParam(name = "harborId") Long harborId){
        try{
            containerServices.retrieveContainerToHarbor(containerId, userId, harborId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Le Conteneur avec  l'id \" + containerId + \" a été retirer avec succès.");

        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/delivery/{id}")
    public String deliveryContainer(@PathVariable Long id, @RequestParam(name = "userId") Long userId){
        String result = containerServices.startDelivery(id, userId);
        switch (result){
            case "NO_PACKAGE_FOR_DELIVERY":
                return "Impossible de démarrer la livraison, pas de colis dans le conteneur.";
            default:
                return "Le Conteneur avec  l'id " + id + " est encours de livraison.";
        }
    }

    @GetMapping("/delivery-received/{id}")
    public String confirmDelivery(@PathVariable Long id, @RequestParam(name = "userId") Long userId){
        String result = containerServices.confirmDelivery(id, userId);
        switch (result){
            case "NO_PACKAGE_FOR_DELIVERY":
                return "Impossible de confirmer la réception, pas de colis dans le conteneur.";
            case "CONTAINER_NOT_IN_PROGRESS":
                return "Le conteneur n'est pas en status INPROGRESS.";
            default:
                return "Le Conteneur avec  l'id " + id + " a ete receptionner avec succès.";
        }
    }
}
