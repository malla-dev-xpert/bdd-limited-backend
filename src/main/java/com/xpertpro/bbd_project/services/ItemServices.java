package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.ItemDto;
import com.xpertpro.bbd_project.dto.Package.PackageResponseDto;
import com.xpertpro.bbd_project.entity.Items;
import com.xpertpro.bbd_project.entity.Packages;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.entity.Warehouse;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.ItemsRepository;
import com.xpertpro.bbd_project.repository.PackageRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItemServices {
    @Autowired
    ItemsRepository itemsRepository;
    @Autowired
    PackageRepository packageRepository;
    @Autowired
    UserRepository userRepository;

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

    public String deleteItem(Long id, Long userId, Long packageId) {
        Optional<Items> optionalItems = itemsRepository.findById(id);
        Optional<Packages> optionalPackages = packageRepository.findById(packageId);
        Optional<UserEntity> optionalUser = userRepository.findById(userId);
        if (optionalItems.isEmpty()) {
            return "ITEM_NOT_FOUND";
        }

        if (optionalPackages.isEmpty()) {
            return "PACKAGE_NOT_FOUND";
        }

        if (optionalUser.isEmpty()) {
            return "USER_NOT_FOUND";
        }

        Items items = optionalItems.get();

        items.setStatus(StatusEnum.DELETE);
        items.setUser(optionalUser.get());
        itemsRepository.save(items);

        return "DELETED";
    }
}
