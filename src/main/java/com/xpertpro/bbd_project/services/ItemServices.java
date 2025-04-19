package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.ItemDto;
import com.xpertpro.bbd_project.dto.Package.PackageResponseDto;
import com.xpertpro.bbd_project.entity.Items;
import com.xpertpro.bbd_project.entity.Packages;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.ItemsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServices {
    @Autowired
    ItemsRepository itemsRepository;

    public List<ItemDto> getItemsByPackageId(Long packageId) {
        List<Items> items = itemsRepository.findByPackagesId(packageId);
        return items.stream()
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                .map(pkg -> {
                    ItemDto dto = new ItemDto();
                    dto.setId(pkg.getId());
                    dto.setQuantity(pkg.getQuantity());
                    dto.setDescription(pkg.getDescription());

                    return dto;
                })
                .collect(Collectors.toList());
    }
}
