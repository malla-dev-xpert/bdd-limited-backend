package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.config.ApiResponsee;
import com.xpertpro.bbd_project.dto.achats.ConfirmItemsDelivery;
import com.xpertpro.bbd_project.dto.achats.CreateAchatDto;
import com.xpertpro.bbd_project.services.AchatServices;
import com.xpertpro.bbd_project.services.LogServices;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("api/v1/achats")
@CrossOrigin("*")
public class AchatController {
    @Autowired
    AchatServices achatServices;
    @Autowired
    LogServices logServices;

    @PostMapping("/create")
    public ResponseEntity<ApiResponsee<String>> createAchat(
            @RequestParam Long clientId,
            @RequestParam Long userId,
            @RequestBody CreateAchatDto dto) {

        try {
            String result = achatServices.createAchatForClient(clientId, userId, dto);
            return ResponseEntity.ok(
                    new ApiResponsee<>(
                            true,
                            "Purchase created successfully",
                            result,
                            null
                    )
            );

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponsee<>(
                            false,
                            e.getMessage(),
                            null,
                            Collections.singletonList(e.getMessage())
                    ));

        } catch (AchatServices.BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponsee<>(
                            false,
                            e.getMessage(),
                            null,
                            Collections.singletonList(e.getMessage())
                    ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponsee<>(
                            false,
                            "An unexpected error occurred",
                            null,
                            Collections.singletonList("SERVER_ERROR")
                    ));
        }
    }

    @PostMapping("/items/confirm-delivery")
    public ResponseEntity<ApiResponsee<Void>> confirmItemDelivery(
            @RequestBody ConfirmItemsDelivery request,
            @RequestHeader("X-User-Id") Long userId) {

        try {
            // Validation des entrées
            if (request.getItemIds() == null || request.getItemIds().isEmpty()) {
                throw new AchatServices.BusinessException("NO_ITEMS_PROVIDED", "Au moins un item doit être fourni");
            }
            if (userId == null) {
                throw new IllegalArgumentException("User ID est requis");
            }

            // Appel du service
            achatServices.confirmItemDelivery(request.getItemIds(), userId);

            return ResponseEntity.ok(
                    new ApiResponsee<>(
                            true,
                            "Livraison confirmée avec succès",
                            null,
                            null
                    )
            );

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponsee<>(
                            false,
                            e.getMessage(),
                            null,
                            Collections.singletonList("NOT_FOUND")
                    ));
        } catch (AchatServices.BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponsee<>(
                            false,
                            e.getMessage(),
                            null,
                            Collections.singletonList(e.getMessage())
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponsee<>(
                            false,
                            "Erreur lors de la confirmation de livraison",
                            null,
                            Collections.singletonList("SERVER_ERROR")
                    ));
        }
    }
}