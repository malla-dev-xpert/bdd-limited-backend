package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.partners.CreatePartnersDto;
import com.xpertpro.bbd_project.dto.user.CreateUserDto;
import com.xpertpro.bbd_project.services.PartnerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/partners")
@CrossOrigin("*")
public class PartnersController {

    @Autowired
    PartnerServices partnerServices;

    @PostMapping("/create")
    public ResponseEntity<String> register(@RequestBody CreatePartnersDto partnersDto) {
        String result = partnerServices.createPartners(partnersDto);
        switch (result) {
            case "EMAIL_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email déjà utilisé par un partenaire !");
            case "PHONE_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Numéro de téléphone déjà enregistré au nom d'un partenaire !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Partenaire ajouté avec succès !");
        }
    }
}
