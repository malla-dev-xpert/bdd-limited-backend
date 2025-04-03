package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.PaymentDto;
import com.xpertpro.bbd_project.entity.Payments;
import com.xpertpro.bbd_project.services.PaymentServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/payments")
@CrossOrigin("*")
public class PaymentsController {
    @Autowired
    PaymentServices paymentServices;

    @PostMapping("/create")
    public ResponseEntity<String> ajouter(@RequestBody PaymentDto paymentsDto, @RequestParam(name = "userId") Long userId) {
        paymentServices.registerPayment(paymentsDto, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body("Paiement ajouté avec succès !");
    }

    @GetMapping()
    public Page<Payments> getAllPayment(@RequestParam(defaultValue = "0") int page) {
        return paymentServices.getAllPayments(page);
    }
}
