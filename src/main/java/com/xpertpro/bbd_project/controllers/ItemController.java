package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.ItemDto;
import com.xpertpro.bbd_project.services.ItemServices;
import org.springframework.beans.factory.annotation.Autowired;
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
}
