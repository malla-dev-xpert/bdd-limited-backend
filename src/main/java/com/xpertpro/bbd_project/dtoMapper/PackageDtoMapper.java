package com.xpertpro.bbd_project.dtoMapper;

import com.xpertpro.bbd_project.dto.Package.PackageCreateDto;
import com.xpertpro.bbd_project.entity.Packages;
import org.springframework.stereotype.Component;

@Component
public class PackageDtoMapper {
    public Packages toEntity(PackageCreateDto dto) {
        Packages pkg = new Packages();
        pkg.setReference(dto.getReference());
        pkg.setWeight(dto.getWeight());
        pkg.setDimensions(dto.getDimensions());
        pkg.setCreatedAt(dto.getCreatedAt());
        return pkg;
    }
}
