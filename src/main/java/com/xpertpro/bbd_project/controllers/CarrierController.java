package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.carrier.CarrierDto;
import com.xpertpro.bbd_project.services.CarrierServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/carriers")
@CrossOrigin("*")
public class CarrierController {

    @Autowired
    CarrierServices carrierServices;

    @PostMapping("/create")
    public ResponseEntity<String> ajouter(@RequestBody CarrierDto carrierDto) {
        String result = carrierServices.createCarrier(carrierDto);
        switch (result) {
            case "CONTACT_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Contact déjà utilisé !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Transporteur ajouté avec succès !");
        }
    }
}
