package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.items.ItemDto;
import com.xpertpro.bbd_project.dto.items.ItemResponseDto;
import com.xpertpro.bbd_project.services.ItemServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/items")
@CrossOrigin("*")
public class ItemController {

    @Autowired
    ItemServices itemServices;

    @GetMapping("/package/{packageId}")
    public List<ItemDto> getItemsByPackageId(@PathVariable Long packageId) {
        return itemServices.getItemsByPackageId(packageId);
    }

    @GetMapping("/customer")
    public List<ItemDto> getItemsByClientId(@RequestParam(name = "clientId") Long clientId) {
        return itemServices.getItemsByClientId(clientId);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id, @RequestParam(name = "userId") Long userId, @RequestParam(name = "clientId") Long clientId){
        String result = itemServices.deleteItem(id, userId, clientId);
        switch (result) {
            case "ITEM_NOT_FOUND":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Article non trouve.");
            case "CLIENT_NOT_FOUND_OR_MISMATCH":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Client non trouve.");
            case "USER_NOT_FOUND":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Utilisateur non trouve.");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Article supprimer avec succès !");
        }
    }

    @GetMapping()
    public ResponseEntity<List<ItemResponseDto>> getAll(@RequestParam(name = "page") int page) {
        return ResponseEntity.ok(itemServices.getAllItem(page));
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<String> updatePackage(
            @PathVariable Long id,
            @RequestParam(name = "packageId")Long packageId,
            @RequestBody ItemDto dto
    ) {
        itemServices.updateItem(id, packageId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Article modifier avec succès !");
    }
}
