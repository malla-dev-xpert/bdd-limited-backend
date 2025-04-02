package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.partners.CreatePartnersDto;
import com.xpertpro.bbd_project.dto.partners.UpdatePartnersDto;
import com.xpertpro.bbd_project.entity.Partners;
import com.xpertpro.bbd_project.services.PartnerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

    @GetMapping()
    public Page<Partners> getAllPartners(@RequestParam(defaultValue = "0") int page) {
        return partnerServices.getAllPartners(page);
    }

    @GetMapping("/{id}")
    public Partners getPartnerById(@PathVariable Long id) {
        return partnerServices.getPartnerById(id);
    }

    @GetMapping("/account-type")
    public Page<Partners> getAllPartnersByType(@RequestParam(defaultValue = "0") int page, @RequestParam String type) {
        return partnerServices.findPartnersByType(page, type);
    }

    @DeleteMapping("/delete/{id}")
    public String deletePartners(@PathVariable Long id){
        partnerServices.deletePartners(id);
        return "Le partenaire avec  l'id " + id + " a été supprimer avec succès.";
    }

    @PutMapping("/update/{id}")
    public UpdatePartnersDto updatePartnersDto(@PathVariable Long id, @RequestBody UpdatePartnersDto updatePartnersDto) {
        return partnerServices.updatePartnersDto(id, updatePartnersDto);
    }
}
