package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.warehouse.WarehouseDto;
import com.xpertpro.bbd_project.entity.UserEntity;
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
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Entrepot ajouté avec succès !");
        }
    }

    @GetMapping()
    public Page<Warehouse> getAllWarehouse(@RequestParam(defaultValue = "0") int page) {
        return warehouseServices.findAllWarehouse(page);
    }
}
