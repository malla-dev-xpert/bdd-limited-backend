package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.partners.PartnerDto;
import com.xpertpro.bbd_project.dto.partners.UpdatePartnersDto;
import com.xpertpro.bbd_project.entity.Partners;
import com.xpertpro.bbd_project.services.PartnerServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/partners")
@CrossOrigin("*")
public class PartnersController {

    @Autowired
    PartnerServices partnerServices;

    @PostMapping("/create")
    public ResponseEntity<String> register(@RequestBody PartnerDto partnersDto, @RequestParam(name = "userId") Long userId) {
        String result = partnerServices.createPartners(partnersDto, userId);
        switch (result) {
            case "EMAIL_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email déjà utilisé par un partenaire !");
            case "PHONE_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Numéro de téléphone déjà enregistré au nom d'un partenaire !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Partenaire ajouté avec succès !");
        }
    }

    @GetMapping("/customer")
    public List<PartnerDto> getAllCustomer(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) String query) {
        return partnerServices.getAllCustomer(page, query);
    }

    @GetMapping("/supplier")
    public List<PartnerDto> getAllSupplier(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) String query) {
        return partnerServices.getAllSupplier(page, query);
    }

    @GetMapping("/{id}")
    public Partners getPartnerById(@PathVariable Long id) {
        return partnerServices.getPartnerById(id);
    }

    @GetMapping("/account-type")
    public Page<PartnerDto> getAllPartnersByType(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam String type,
            @RequestParam(required = false) String query) {
        return partnerServices.findPartnersByType(page, type, query);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable(name = "id") Long partnerId, @RequestParam(name = "userId")Long userId) {
        String result = partnerServices.deletePartner(partnerId, userId);
        switch (result) {
            case "PARTNER_NOT_FOUND":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Partenaire non trouvé !");
            case "PACKAGE_FOUND":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Impossible de supprimer, des colis existent pour ce partenaire.");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Partenaire supprimé avec succès !");
        }
    }

    @PutMapping("/update/{id}")
    public UpdatePartnersDto updatePartnersDto(@PathVariable Long id, @RequestBody UpdatePartnersDto updatePartnersDto) {
        return partnerServices.updatePartnersDto(id, updatePartnersDto);
    }
}
