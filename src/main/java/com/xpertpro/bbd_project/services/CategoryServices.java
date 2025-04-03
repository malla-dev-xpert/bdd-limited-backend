package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.CategoryDto;
import com.xpertpro.bbd_project.entity.Category;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.CategoryRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CategoryServices {

    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    UserRepository userRepository;

    public String createCategory(CategoryDto categoryDto, Long userId) {
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (categoryRepository.findByName(categoryDto.getName()).isPresent()) {
            return "NAME_EXIST";
        }

        if(optionalUser.isPresent()){
            Category category = new Category();

            category.setName(categoryDto.getName());
            category.setDescription(categoryDto.getDescription());
            category.setCreatedAt(categoryDto.getCreatedAt());
            category.setUser(optionalUser.get());

            categoryRepository.save(category);
            return "SUCCESS";
        }else{
            throw new RuntimeException("User not found with ID: " + userId);
        }

    }

    public String updateCategory(Long id, CategoryDto categoryDto, Long userId) {
        Category newCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categorie non trouvé"));
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (categoryRepository.findByName(categoryDto.getName()).isPresent()) {
            return "NAME_EXIST";
        }

        if(optionalUser.isPresent()){
            if (categoryDto.getDescription() != null) newCategory.setDescription(categoryDto.getDescription());
            if (categoryDto.getName() != null) newCategory.setName(categoryDto.getName());
            newCategory.setEditedAt(categoryDto.getEditedAt());

            categoryRepository.save(newCategory);
            return "SUCCESS";
        }else{
            throw new RuntimeException("User not found with ID: " + id);
        }
    }

    public Page<Category> getAllCategory(int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").ascending());
        return categoryRepository.findByStatus(StatusEnum.CREATE, pageable);
    }

    public Category getCategoryById(Long id) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (optionalCategory.isPresent()) {
            Category category = optionalCategory.get();
            return category;
        } else {
            throw new RuntimeException("Categorie non trouvé avec l'ID : " + id);
        }
    }

    public String deleteCategory(Long id, Long userId) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with ID: " + id));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        category.setStatus(StatusEnum.DELETE);
        category.setUser(user);
        categoryRepository.save(category);
        return "Category deleted successfully";
    }
}
