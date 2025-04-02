package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.carrier.CarrierDto;
import com.xpertpro.bbd_project.entity.Carriers;
import com.xpertpro.bbd_project.services.CarrierServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<String> ajouter(@RequestBody CarrierDto carrierDto, @RequestParam(name = "userId") Long userId) {
        String result = carrierServices.createCarrier(carrierDto, userId);
        switch (result) {
            case "CONTACT_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Contact déjà utilisé !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Transporteur ajouté avec succès !");
        }
    }

    @GetMapping()
    public Page<Carriers> getAllCarriers(@RequestParam(defaultValue = "0") int page) {
        return carrierServices.getAllCarriers(page);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateHarbor
            (@PathVariable Long id,
             @RequestBody CarrierDto carrierDto,
             @RequestParam(name = "userId") Long userId)
    {
        String result = carrierServices.updateCarrier(id,carrierDto, userId);
        switch (result) {
            case "CONTACT_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce contact existe déjà !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Transporteur modifier avec succès !");
        }
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id, @RequestParam(name = "userId") Long userId){
        carrierServices.deleteCarrier(id, userId);
        return "Le transporteur avec  l'id " + id + " a été supprimé avec succès.";
    }
}
