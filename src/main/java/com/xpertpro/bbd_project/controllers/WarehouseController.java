package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.warehouse.WarehouseDto;
import com.xpertpro.bbd_project.entity.Warehouse;
import com.xpertpro.bbd_project.services.WarehouseServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/warehouses")
@CrossOrigin("*")
public class WarehouseController {

    @Autowired
    WarehouseServices warehouseServices;

    @PostMapping("/create")
    public ResponseEntity<String> createWarehouse(@RequestBody WarehouseDto warehouseDto, @RequestParam(name = "userId") Long userId) {
        String result = warehouseServices.createWarehouse(warehouseDto, userId);
        switch (result) {
            case "NAME_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Cet entrepot existe déjà !");
            case "ADRESS_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Un entrepôt existe déjà avec cette adresse !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Entrepot ajouté avec succès !");
        }
    }

    @GetMapping()
    public Page<Warehouse> getAllWarehouse(@RequestParam(defaultValue = "0") int page) {
        return warehouseServices.findAllWarehouse(page);
    }

    @GetMapping("/{id}")
    public Warehouse getWarehouseById(@PathVariable Long id) {
        return warehouseServices.getWarehousById(id);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateWarehouse(@PathVariable Long id, @RequestBody WarehouseDto newWarehouse, @RequestParam(name = "userId") Long userId){
        String result = warehouseServices.updateWarehouse(id,newWarehouse, userId);
        switch (result) {
            case "NAME_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Cet entrepot existe déjà !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Entrepot modifier avec succès !");
        }
    }

    @DeleteMapping("/delete/{id}")
    public String deleteWarehouse(@PathVariable Long id, @RequestParam(name = "userId") Long userId){
        warehouseServices.deleteWarehouse(id, userId);
        return "L'entrepôt avec  l'id " + id + " a été supprimé avec succès.";
    }
}
