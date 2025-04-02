package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.carrier.CarrierDto;
import com.xpertpro.bbd_project.dto.containers.ContainersDto;
import com.xpertpro.bbd_project.services.ContainerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/containers")
@CrossOrigin("*")
public class ContainersController {

    @Autowired
    ContainerServices containerServices;

    @PostMapping("/create")
    public ResponseEntity<String> addContainers(@RequestBody ContainersDto containersDto, @RequestParam(name = "userId") Long userId) {
        String result = containerServices.createContainer(containersDto, userId);
        switch (result) {
            case "REF_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Numéro d'identification déjà utilisé !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Le contenu a été ajouté avec succès!");
        }
    }
}
