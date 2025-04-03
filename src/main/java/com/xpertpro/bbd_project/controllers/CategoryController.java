package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.dto.CategoryDto;
import com.xpertpro.bbd_project.entity.Category;
import com.xpertpro.bbd_project.services.CategoryServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/category")
@CrossOrigin("*")
public class CategoryController {
    @Autowired
    CategoryServices categoryServices;

    @PostMapping("/create")
    public ResponseEntity<String> addCategory(@RequestBody CategoryDto categoryDto, @RequestParam(name = "userId") Long userId) {
        String result = categoryServices.createCategory(categoryDto, userId);
        switch (result) {
            case "NAME_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Cette categorie existe déjà!");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Le categorie a été ajouté avec succès!");
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateCategory
            (@PathVariable Long id,
             @RequestBody CategoryDto categoryDto,
             @RequestParam(name = "userId") Long userId)
    {
        String result = categoryServices.updateCategory(id,categoryDto, userId);
        switch (result) {
            case "NAME_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Cette categorie existe déjà !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Categorie modifier avec succès !");
        }
    }

    @GetMapping()
    public Page<Category> getAllCategorie(@RequestParam(defaultValue = "0") int page) {
        return categoryServices.getAllCategory(page);
    }

    @GetMapping("/{id}")
    public Category getCategoryById(@PathVariable Long id){
        return categoryServices.getCategoryById(id);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, @RequestParam(name = "userId") Long userId){
        categoryServices.deleteCategory(id, userId);
        return "La categorie avec  l'id " + id + " a été supprimé avec succès.";
    }
}
