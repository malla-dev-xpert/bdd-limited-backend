package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.achats.VersementDto;
import com.xpertpro.bbd_project.services.VersementServices;
import org.springframework.beans.factory.annotation.Autowired;
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
            @RequestBody VersementDto dto
    ){
        return versementServices.newVersement(userId, partnerId, dto);
    }

    @GetMapping()
    public List<VersementDto> getAll(@RequestParam(name = "page") int page){
        return versementServices.getAll(page);
    }

}
