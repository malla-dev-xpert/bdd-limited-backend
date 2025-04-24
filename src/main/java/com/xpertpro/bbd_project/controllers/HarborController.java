package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.harbor.HarborDto;
import com.xpertpro.bbd_project.entity.Harbor;
import com.xpertpro.bbd_project.services.HarborServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("api/v1/harbors")
@CrossOrigin("*")
public class HarborController {
    @Autowired
    HarborServices harborServices;

    @PostMapping("/create")
    public ResponseEntity<String> createHarbor(@RequestBody HarborDto harborDto, @RequestParam(name = "userId") Long userId){
        String result = harborServices.createHarbor(harborDto, userId);
        switch (result) {
            case "NAME_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Nom de port déjà utilisé !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Port ajouté avec succès !");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateHarbor
            (@PathVariable Long id,
             @RequestBody HarborDto newHarbor,
             @RequestParam(name = "userId") Long userId)
    {
        String result = harborServices.updateHarbor(id,newHarbor, userId);
        switch (result) {
            case "NAME_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce port existe déjà !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Port modifier avec succès !");
        }
    }

    @DeleteMapping("/delete/{id}")
    public String deleteHarbor(@PathVariable Long id, @RequestParam(name = "userId") Long userId){
        harborServices.deleteHarbor(id, userId);
        return "Le port avec  l'id " + id + " a été supprimé avec succès.";
    }

    @DeleteMapping("/disable/{id}")
    public String disableHarbor(@PathVariable Long id, @RequestParam(name = "userId") Long userId){
        harborServices.disableHarbor(id, userId);
        return "Le port avec  l'id " + id + " a été desactiver avec succès.";
    }

    @GetMapping()
    public List<HarborDto> getAllHarbor(@RequestParam(defaultValue = "0") int page) {
        return harborServices.getAllHarbor(page);
    }

    @GetMapping("/{id}")
    public Harbor getHarborById(@PathVariable Long id) {
        return harborServices.getHarborById(id);
    }
}
