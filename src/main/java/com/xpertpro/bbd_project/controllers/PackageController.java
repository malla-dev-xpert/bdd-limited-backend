package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.Package.PackageCreateDto;
import com.xpertpro.bbd_project.dto.Package.PackageResponseDto;
import com.xpertpro.bbd_project.entity.Packages;
import com.xpertpro.bbd_project.services.PackageServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/packages")
@CrossOrigin("*")
public class PackageController {

    @Autowired
    PackageServices packageServices;

    @PostMapping("/create")
    public ResponseEntity<String> createPackage(
            @RequestParam(name = "warehouseId") Long warehouseId,
            @RequestParam(name = "userId") Long userId,
            @RequestBody PackageCreateDto pkg) {
        String result = packageServices.createPackage(warehouseId, pkg, userId);
        switch (result) {
            case "DUPLICATE_REFERENCE":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ce colis existe deja !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Colis ajouté avec succès !");
        }
    }

    @GetMapping("/unassigned")
    public List<PackageResponseDto> getUnassignedPackages(@RequestParam(name = "warehouseId") Long warehouseId) {
        return packageServices.listUnassignedPackages(warehouseId);
    }

    @GetMapping("/warehouse")
    public List<PackageResponseDto> getPackageByWarehouse(@RequestParam(name = "warehouseId") Long warehouseId) {
        return packageServices.getPackagesByWarehouse(warehouseId);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePackage(
            @PathVariable Long id,
            @RequestParam(name = "userId") Long userId) {
        packageServices.deleteDevises(id, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Colis supprimer avec succès !");
    }
}
