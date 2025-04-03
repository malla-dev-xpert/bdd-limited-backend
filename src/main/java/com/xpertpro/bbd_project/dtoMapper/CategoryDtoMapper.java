package com.xpertpro.bbd_project.dtoMapper;

import com.xpertpro.bbd_project.dto.CategoryDto;
import com.xpertpro.bbd_project.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryDtoMapper {
    public Category toEntity(CategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }
}
