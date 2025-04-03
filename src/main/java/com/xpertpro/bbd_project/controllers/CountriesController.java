package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.CountriesDto;
import com.xpertpro.bbd_project.entity.Countries;
import com.xpertpro.bbd_project.services.CountriesServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/countries")
@CrossOrigin("*")
public class CountriesController {

    @Autowired
    CountriesServices countriesServices;

    @PostMapping("/create")
    public ResponseEntity<String> addCountries(@RequestBody CountriesDto countriesDto, @RequestParam(name = "userId") Long userId) {
        String result = countriesServices.createCountries(countriesDto, userId);
        switch (result) {
            case "NAME_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce nom de pays existe déjà!");
            case "ISO_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce code iso existe déjà!");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Le Pays a été ajouté avec succès!");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateCountry
            (@PathVariable Long id,
             @RequestBody CountriesDto countriesDto,
             @RequestParam(name = "userId") Long userId)
    {
        String result = countriesServices.updateCountries(id,countriesDto, userId);
        switch (result) {
            case "NAME_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce nom de pays existe déjà !");
            case "ISO_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce code iso existe déjà !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Pays modifier avec succès !");
        }
    }

    @GetMapping()
    public Page<Countries> getAllCountries(@RequestParam(defaultValue = "0") int page) {
        return countriesServices.getAllCountries(page);
    }

    @GetMapping("/{id}")
    public Countries getCountryById(@PathVariable Long id){
        return countriesServices.getCountryById(id);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteCountry(@PathVariable Long id, @RequestParam(name = "userId") Long userId){
        countriesServices.deleteCountry(id, userId);
        return "Le Pays avec  l'id " + id + " a été supprimé avec succès.";
    }
}
