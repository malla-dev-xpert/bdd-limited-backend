package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.ItemDto;
import com.xpertpro.bbd_project.dto.Package.PackageCreateDto;
import com.xpertpro.bbd_project.dto.Package.PackageResponseDto;
import com.xpertpro.bbd_project.entity.Packages;
import com.xpertpro.bbd_project.services.PackageServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/packages")
@CrossOrigin("*")
public class PackageController {

    @Autowired
    PackageServices packageServices;

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createPackage(
            @RequestParam(name = "warehouseId") Long warehouseId,
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "partnerId") Long clientId,
            @RequestBody PackageCreateDto pkg) {

        Packages newPackages = packageServices.createPackageWithItems(warehouseId, userId, clientId, pkg);

        Long createdId = newPackages.getId();

        if (createdId == -1L) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", "Ce colis existe déjà !")
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("id", createdId, "message", "Colis ajouté avec succès !")
        );
    }


    @GetMapping("/unassigned")
    public List<PackageResponseDto> getUnassignedPackages(@RequestParam(name = "warehouseId") Long warehouseId) {
        return packageServices.listUnassignedPackages(warehouseId);
    }

    @GetMapping("/warehouse")
    public List<PackageResponseDto> getPackageByWarehouse(@RequestParam(name = "warehouseId") Long warehouseId) {
        return packageServices.getPackagesByWarehouse(warehouseId);
    }

    @GetMapping()
    public List<PackageResponseDto> findAll(@RequestParam(defaultValue = "0") int page) {
        return packageServices.getAllPackages(page);
    }

    @GetMapping("/received")
    public List<PackageResponseDto> findAllPackagesReceived(@RequestParam(defaultValue = "0") int page) {
        return packageServices.getAllPackagesReceived(page);
    }

    @DeleteMapping("/receive/{id}")
    public String receivePackage(
            @PathVariable Long id,
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "warehouseId") Long warehouseId) {
        packageServices.receivePackages(id, userId, warehouseId);
        return "Colis recu avec succès !";
    }

    @DeleteMapping("/{id}/container/{containerId}/delete")
    public String deleteOnContainer(
            @PathVariable(name = "id") Long id,
            @RequestParam(name = "userId") Long userId,
            @PathVariable(name = "containerId") Long containerId) {
        packageServices.deletePackagesOnContainer(id, userId, containerId);
        return "Colis retiré du conteneur" + containerId + " avec succès !";
    }

    @DeleteMapping("/delete/{id}")
    public String deltePackage(
            @PathVariable Long id,
            @RequestParam(name = "userId") Long userId) {
        packageServices.deletePackages(id, userId);
        return "Colis supprimer avec succès !";
    }

    @PostMapping("/{packageId}/add-items")
    public ResponseEntity<String> addItems(
            @PathVariable Long packageId,
            @RequestBody List<ItemDto> items,
            @RequestParam(name = "userId") Long userId
    ) {
        packageServices.addItemsToPackage(packageId, items, userId);
        return ResponseEntity.ok("Articles ajoutés au colis");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updatePackage(
            @PathVariable Long id,
            @RequestParam(name = "userId")Long userId,
            @RequestBody PackageResponseDto dto
    ) {
        String result = packageServices.updatePackage(id, userId, dto);
        switch (result) {
            case "DUPLICATE_REFERENCE":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Nom de colis déjà utilisé !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Colis modifier avec succès !");
        }
    }

}
