package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.items.ItemDto;
import com.xpertpro.bbd_project.dto.items.ItemResponseDto;
import com.xpertpro.bbd_project.entity.*;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.AchatRepository;
import com.xpertpro.bbd_project.repository.ItemsRepository;
import com.xpertpro.bbd_project.repository.PackageRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
    @Autowired
    AchatRepository achatRepository;

    public List<ItemDto> getItemsByPackageId(Long packageId) {
        List<Items> items = itemsRepository.findByPackagesId(packageId);
        return items.stream()
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                .map(pkg -> {
                    ItemDto dto = new ItemDto();
                    dto.setId(pkg.getId());
                    dto.setQuantity(pkg.getQuantity());
                    dto.setDescription(pkg.getDescription());
                    dto.setUnitPrice(pkg.getUnitPrice());
                    dto.setSupplierName(pkg.getSupplier() != null ? pkg.getSupplier().getFirstName() + " " + pkg.getSupplier().getLastName() : null);
                    dto.setSupplierPhone(pkg.getSupplier() != null ? pkg.getSupplier().getPhoneNumber() : null);
                    dto.setStatus(pkg.getStatus().name());

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ItemDto> getItemsByClientId(Long clientId) {
        // 1. Récupérer tous les achats du client
        List<Achats> achats = achatRepository.findByClientId(clientId);

        // 2. Récupérer tous les items de ces achats
        List<Items> items = achats.stream()
                .flatMap(achat -> achat.getItems().stream())
                .collect(Collectors.toList());

        // 3. Filtrer et mapper vers ItemDto
        return items.stream()
                .filter(item -> item.getStatus() == StatusEnum.RECEIVED)
                .filter(item -> item.getPackages() == null || item.getPackages().getId() == null)
                .map(item -> {
                    ItemDto itemDto = new ItemDto();
                    itemDto.setId(item.getId());
                    itemDto.setDescription(item.getDescription());
                    itemDto.setQuantity(item.getQuantity());
                    itemDto.setUnitPrice(item.getUnitPrice());

                    // Info fournisseur
                    if(item.getSupplier() != null) {
                        itemDto.setSupplierName(item.getSupplier().getFirstName() + " " + item.getSupplier().getLastName());
                        itemDto.setSupplierPhone(item.getSupplier().getPhoneNumber());
                    }

                    itemDto.setStatus(item.getStatus().name());
                    itemDto.setTotalPrice(item.getTotalPrice());
                    itemDto.setPackageId(item.getPackages() != null ? item.getPackages().getId() : null);

                    return itemDto;
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

    public String updateItem(Long id, Long packageId, ItemDto dto) {
        packageRepository.findById(packageId)
                .orElseThrow(() -> new EntityNotFoundException("Package not found with id: " + id));
        Items existingItem = itemsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item not found with id: " + id));

        existingItem.setQuantity(dto.getQuantity());
        existingItem.setDescription(dto.getDescription());
        existingItem.setUnitPrice(dto.getUnitPrice());

        itemsRepository.save(existingItem);

        return "Item updated";
    }

    public List<ItemResponseDto> getAllItem(int page) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Items> items = itemsRepository.findByStatusNot(StatusEnum.DELETE, pageable);

        return items.stream()
//                .filter(item -> item.getStatus() == StatusEnum.CREATE)
                .sorted(Comparator.comparing(Items::getCreatedAt).reversed())
                .map(item -> {
                    ItemResponseDto dto = new ItemResponseDto();
                    dto.setItemId(item.getId());
                    dto.setDescription(item.getDescription());
                    dto.setQuantity(item.getQuantity());
                    dto.setUnitPrice(item.getUnitPrice());
                    dto.setAchatDate(item.getAchats() != null ? item.getAchats().getCreatedAt() : null);
                    dto.setStatus(item.getStatus().name());
                    dto.setSupplierId(item.getSupplier() != null ? item.getSupplier().getId() : null);
                    dto.setSupplierName(item.getSupplier() != null ? item.getSupplier().getFirstName() + " " + item.getSupplier().getLastName() : null);
                    dto.setSupplierPhone(item.getSupplier() != null ? item.getSupplier().getPhoneNumber() : null);

                    return dto;
                })
                .collect(Collectors.toList());
    }

}
