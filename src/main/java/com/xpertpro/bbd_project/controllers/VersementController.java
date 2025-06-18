package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.achats.VersementDto;
import com.xpertpro.bbd_project.services.ContainerPackageService;
import com.xpertpro.bbd_project.services.VersementServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/versements")
@CrossOrigin("*")
public class VersementController {
    @Autowired
    VersementServices versementServices;


    @PostMapping("/new")
    public String create(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "partnerId")  Long partnerId,
            @RequestParam(name = "deviseId")  Long deviseId,
            @RequestBody VersementDto dto
    ){
        return versementServices.newVersement(userId, partnerId,deviseId, dto);
    }

    @GetMapping()
    public List<VersementDto> getAll(@RequestParam(name = "page") int page){
        return versementServices.getAll(page);
    }

    @GetMapping("/client")
    public ResponseEntity<List<VersementDto>> getVersementsByClient(
            @RequestParam(name = "clientId") Long clientId,
            @RequestParam(defaultValue = "0") int page) {

        return ResponseEntity.ok(versementServices.getByClientId(clientId, page));
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateVersement(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam Long clientId,
            @RequestBody VersementDto dto) {

        try {
            boolean updated = versementServices.updateVersement(id, userId, clientId, dto);
            return ResponseEntity.ok().build();
        } catch (ContainerPackageService.ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de la mise à jour");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteVersement(
            @PathVariable Long id,
            @RequestParam(name = "userId") Long userId
    ) {
        String result = versementServices.deleteVersement(id, userId);
        switch (result){
            case "IMPOSSIBLE":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Impossible de supprimer: des achats sont déjà associés à ce versement");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Paiement supprimer avec succès.");
        }

    }

}
