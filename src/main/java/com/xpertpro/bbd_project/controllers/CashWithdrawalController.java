package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.CashWithdrawalDto;
import com.xpertpro.bbd_project.services.CashWithdrawalServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("api/v1/")
@CrossOrigin("*")
public class CashWithdrawalController {
    private final CashWithdrawalServices cashWithdrawalService;

    public CashWithdrawalController(CashWithdrawalServices cashWithdrawalService) {
        this.cashWithdrawalService = cashWithdrawalService;
    }

    @PostMapping("versement/retrait")
    public ResponseEntity<?> createRetrait(@RequestBody CashWithdrawalDto dto) {
        try {
            String response = cashWithdrawalService.createRetrait(dto);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());
        }
    }
}
