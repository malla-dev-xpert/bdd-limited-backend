package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.items.ItemDto;
import com.xpertpro.bbd_project.dto.items.ItemResponseDto;
import com.xpertpro.bbd_project.entity.*;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
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
    @Autowired
    LogServices logServices;
    @Autowired
    PartnerRepository partnerRepository;

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
                    dto.setTotalPrice(pkg.getTotalPrice());
                    dto.setSalesRate(pkg.getSalesRate());

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
                    itemDto.setSalesRate(item.getSalesRate());

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

    public String deleteItem(Long id, Long userId, Long clientId) {
        Optional<Items> optionalItems = itemsRepository.findById(id);
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (optionalItems.isEmpty()) {
            return "ITEM_NOT_FOUND";
        }

        if (optionalUser.isEmpty()) {
            return "USER_NOT_FOUND";
        }

        Items item = optionalItems.get();
        Achats achat = item.getAchats();

        // Vérification du client
        if (achat == null || achat.getClient() == null || !achat.getClient().getId().equals(clientId)) {
            return "CLIENT_NOT_FOUND_OR_MISMATCH";
        }

        // Vérification si c'est le seul item dans l'achat
        boolean isLastItem = achat.getItems().size() == 1;

        item.setStatus(StatusEnum.DELETE);
        item.setUser(optionalUser.get());
        itemsRepository.save(item);

        // Si c'était le dernier item, supprimer l'achat
        if (isLastItem) {
            achat.setStatus(StatusEnum.DELETE);
            achatRepository.save(achat);
            return "DELETED_WITH_ACHAT";
        }

        logServices.logAction(optionalUser.get(), "SUPPRESSION_ARTICLE", "ITEMS", id);

        return "DELETED";
    }

    @Transactional
    public String updateItem(Long itemId, Long userId, ItemDto request) {
        // 1. Vérification de l'existence de l'item et de l'utilisateur
        Items item = itemsRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("ITEM_NOT_FOUND"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        // 2. Vérification que l'item appartient bien au client spécifié
        if (!item.getAchats().getClient().getId().equals(request.getClientId())) {
            throw new RuntimeException("CLIENT_MISMATCH");
        }

        // 3. Mise à jour des champs modifiables
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }

        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
            item.setTotalPrice(item.getQuantity() * item.getUnitPrice());
        }

        if (request.getUnitPrice() != null) {
            item.setUnitPrice(request.getUnitPrice());
            item.setTotalPrice(item.getQuantity() * item.getUnitPrice());
        }

        if (request.getSalesRate() != null) {
            item.setSalesRate(request.getSalesRate());
        }

        if (request.getInvoiceNumber() != null) {
            item.setInvoiceNumber(request.getInvoiceNumber());
        }

        if (request.getSupplierId() != null) {
            Partners supplier = partnerRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new RuntimeException("SUPPLIER_NOT_FOUND"));
            item.setSupplier(supplier);
        }

        // Mise à jour de l'utilisateur qui a modifié l'article
        item.setUser(user);

        // Sauvegarde des modifications
        itemsRepository.save(item);

        // Mise à jour du montant total de l'achat parent
        updateAchatTotal(item.getAchats());

        // Journalisation
        logServices.logAction(user, "MODIFICATION_ARTICLE", "ITEMS", itemId);

        return "SUCCESS";
    }

    private void updateAchatTotal(Achats achat) {
        double total = achat.getItems().stream()
                .filter(i -> i.getStatus() != StatusEnum.DELETE)
                .mapToDouble(Items::getTotalPrice)
                .sum();

        achat.setMontantTotal(total);
        achatRepository.save(achat);
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
                    dto.setTotalPrice(item.getTotalPrice());
                    dto.setSalesRate(item.getSalesRate());

                    return dto;
                })
                .collect(Collectors.toList());
    }

}
