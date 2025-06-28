package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.PackageDto;
import com.xpertpro.bbd_project.dtoMapper.PackageDtoMapper;
import com.xpertpro.bbd_project.entity.*;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PackageServices {
    @Autowired
    PackageRepository packageRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PartnerRepository clientRepo;
    @Autowired
    PackageDtoMapper packageDtoMapper;
    @Autowired
    LogServices logServices;
    @Autowired
    ContainersRepository containersRepository;
    @Autowired
    WarehouseRepository warehouseRepository;
    @Autowired
    ItemsRepository itemsRepository;

    @Transactional
    public String create(PackageDto dto, Long clientId, Long userId, Long containerId, Long warehouseId) {
        // 1. Validation des entités existantes
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Partners client = clientRepo.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        Containers container = containersRepository.findById(containerId)
                .orElseThrow(() -> new EntityNotFoundException("Container not found"));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        // 2. Validation des items
        if (dto.getItemIds() == null || dto.getItemIds().isEmpty()) {
            throw new AchatServices.BusinessException("NO_ITEMS_SELECTED", "At least one item must be selected for the package");
        }

        // 3. Création du colis
        Packages packages = packageDtoMapper.toEntity(dto);
        packages.setCreatedAt(LocalDateTime.now());
        packages.setClient(client);
        packages.setCreatedBy(user);
        packages.setContainer(container);
        packages.setWarehouse(warehouse);

        // 4. Sauvegarde initiale pour obtenir l'ID
        Packages newPackage = packageRepository.save(packages);

        // 5. Gestion des items sélectionnés
        List<Items> itemsToPackage = itemsRepository.findAllById(dto.getItemIds());

        // Vérification que tous les items existent
        if (itemsToPackage.size() != dto.getItemIds().size()) {
            throw new EntityNotFoundException("Some items were not found");
        }

        // Vérification que les items appartiennent bien au client
        itemsToPackage.forEach(item -> {
            if (!item.getAchats().getClient().getId().equals(clientId)) {
                throw new AchatServices.BusinessException("ITEM_CLIENT_MISMATCH",
                        "Item with ID " + item.getId() + " does not belong to client " + clientId);
            }

            // Vérification que l'item n'est pas déjà dans un autre colis
            if (item.getPackages() != null) {
                throw new AchatServices.BusinessException("ITEM_ALREADY_PACKAGED",
                        "Item with ID " + item.getId() + " is already in another package");
            }

            // Associer l'item au colis
            item.setPackages(newPackage);
        });

        // Sauvegarder les modifications sur les items
        itemsRepository.saveAll(itemsToPackage);

        // 7. Logging
        logServices.logAction(
                user,
                "AJOUT_COLIS",
                "Packages",
                newPackage.getId());

        return "SUCCESS";
    }


    public List<PackageDto> getAll(int page, String query) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Packages> expeditions = packageRepository.findByStatusNot(StatusEnum.DELETE, pageable);

        if (query != null && !query.isEmpty()) {
            expeditions = packageRepository.findByStatusAndSearchQuery(
                    StatusEnum.CREATE,
                    "%" + query.toLowerCase() + "%",
                    pageable
            );
        } else {
            expeditions = packageRepository.findByStatusNot(StatusEnum.DELETE, pageable);
        }


        return expeditions.stream()
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(Packages::getCreatedAt).reversed())
                .map(pkg -> {
                    PackageDto dto = new PackageDto();
                    dto.setId(pkg.getId());
                    dto.setRef(pkg.getRef());
                    dto.setWeight(pkg.getWeight());
                    dto.setWeight(pkg.getWeight());
                    dto.setCbn(pkg.getCbn());
                    dto.setStartDate(pkg.getStartDate());
                    dto.setArrivalDate(pkg.getArrivalDate());
                    dto.setStatus(pkg.getStatus().name());
                    dto.setDestinationCountry(pkg.getDestinationCountry());
                    dto.setExpeditionType(pkg.getExpeditionType());
                    dto.setItemQuantity(pkg.getItemQuantity());
                    dto.setStartCountry(pkg.getStartCountry());
                    dto.setWarehouseId(pkg.getWarehouse() != null ? pkg.getWarehouse().getId() : null);
                    dto.setWarehouseName(pkg.getWarehouse() != null ? pkg.getWarehouse().getName() : null);
                    dto.setWarehouseAddress(pkg.getWarehouse() != null ? pkg.getWarehouse().getAdresse() : null);
                    dto.setClientId(pkg.getClient() != null ? pkg.getClient().getId() : null);
                    dto.setClientName(pkg.getClient() != null
                            ? pkg.getClient().getFirstName() + " " + pkg.getClient().getLastName()
                            : null);
                    dto.setClientPhone(pkg.getClient() != null
                            ? pkg.getClient().getPhoneNumber()
                            : null);

                    return dto;
                })
                .collect(Collectors.toList());

    }

    public List<PackageDto> getAllEnAttente(int page) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Packages> expeditions = packageRepository.findByContainerIsNull(pageable);

        return expeditions.stream()
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                .filter(pkg -> pkg.getContainer() == null)
                .sorted(Comparator.comparing(Packages::getCreatedAt).reversed())
                .map(pkg -> {
                    PackageDto dto = new PackageDto();
                    dto.setId(pkg.getId());
                    dto.setRef(pkg.getRef());
                    dto.setWeight(pkg.getWeight());
                    dto.setWeight(pkg.getWeight());
                    dto.setCbn(pkg.getCbn());
                    dto.setStartDate(pkg.getStartDate());
                    dto.setArrivalDate(pkg.getArrivalDate());
                    dto.setStatus(pkg.getStatus().name());
                    dto.setDestinationCountry(pkg.getDestinationCountry());
                    dto.setExpeditionType(pkg.getExpeditionType());
                    dto.setItemQuantity(pkg.getItemQuantity());
                    dto.setStartCountry(pkg.getStartCountry());
                    dto.setWarehouseId(pkg.getWarehouse() != null ? pkg.getWarehouse().getId() : null);
                    dto.setWarehouseName(pkg.getWarehouse() != null ? pkg.getWarehouse().getName() : null);
                    dto.setWarehouseAddress(pkg.getWarehouse() != null ? pkg.getWarehouse().getAdresse() : null);
                    dto.setClientId(pkg.getClient() != null ? pkg.getClient().getId() : null);
                    dto.setClientName(pkg.getClient() != null
                            ? pkg.getClient().getFirstName() + " " + pkg.getClient().getLastName()
                            : null);
                    dto.setClientPhone(pkg.getClient() != null
                            ? pkg.getClient().getPhoneNumber()
                            : null);

                    return dto;
                })
                .collect(Collectors.toList());

    }

    @Transactional
    public void startExpedition(Long expeditionId) {
        Packages expedition = packageRepository.findById(expeditionId)
                .orElseThrow(() -> new EntityNotFoundException("Expédition introuvable avec l'ID : " + expeditionId));

        if (expedition.getStatus() == StatusEnum.INPROGRESS) {
            throw new IllegalStateException("L'expédition est déjà en cours.");
        }

        if (expedition.getStatus() != StatusEnum.PENDING) {
            throw new IllegalStateException("Impossible de démarrer une expédition qui n'est pas en attente.");
        }

        expedition.setStatus(StatusEnum.INPROGRESS);
        expedition.setEditedAt(LocalDateTime.now());
        packageRepository.save(expedition);
    }

    @Transactional
    public void confirmExpedition(Long expeditionId) {
        Packages expedition = packageRepository.findById(expeditionId)
                .orElseThrow(() -> new EntityNotFoundException("Expédition introuvable avec l'ID : " + expeditionId));

        if (expedition.getStatus() == StatusEnum.DELIVERED) {
            throw new IllegalStateException("L'expédition est déjà en arrivé.");
        }

        if (expedition.getStatus() != StatusEnum.INPROGRESS) {
            throw new IllegalStateException("Impossible de confirmer la réception de l'expédition. Elle n'est pas en transit.");
        }

        expedition.setStatus(StatusEnum.DELIVERED);
        expedition.setEditedAt(LocalDateTime.now());
        packageRepository.save(expedition);
    }

    @Transactional
    public void receivedExpedition(Long expeditionId) {
        Packages expedition = packageRepository.findById(expeditionId)
                .orElseThrow(() -> new EntityNotFoundException("Expédition introuvable avec l'ID : " + expeditionId));

        if (expedition.getStatus() == StatusEnum.RECEIVED) {
            throw new IllegalStateException("L'expédition est déjà livrée.");
        }

        if (expedition.getStatus() != StatusEnum.DELIVERED) {
            throw new IllegalStateException("Impossible de confirmer la livraison de l'expédition. Elle n'est pas arrivée a destination.");
        }

        expedition.setStatus(StatusEnum.RECEIVED);
        expedition.setEditedAt(LocalDateTime.now());
        packageRepository.save(expedition);
    }

    @Transactional
    public void deleteExpedition(Long expeditionId) {
        Packages expedition = packageRepository.findById(expeditionId)
                .orElseThrow(() -> new EntityNotFoundException("Expédition introuvable avec l'ID : " + expeditionId));

        if (expedition.getStatus() == StatusEnum.DELETE) {
            throw new IllegalStateException("L'expédition est déjà supprimer.");
        }

        expedition.setStatus(StatusEnum.DELETE);
        expedition.setEditedAt(LocalDateTime.now());
        packageRepository.save(expedition);
    }

    public String updateExpedition(Long id, PackageDto newExpedition, Long userId) {
        Optional<Packages> optionalExpedition = packageRepository.findById(id);
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (optionalExpedition.isEmpty()) {
            throw new RuntimeException("Expedition not found with ID: " + id);
        }

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        Packages expedition = optionalExpedition.get();
        UserEntity user = optionalUser.get();

        // Mise à jour des champs de base
        if (newExpedition.getExpeditionType() != null) {
            expedition.setExpeditionType(newExpedition.getExpeditionType());
        }
        if (newExpedition.getWeight() != 0) {
            expedition.setWeight(newExpedition.getWeight());
        }
        if (newExpedition.getItemQuantity() != 0) {
            expedition.setItemQuantity(newExpedition.getItemQuantity());
        }
        if (newExpedition.getCbn() != 0) {
            expedition.setCbn(newExpedition.getCbn());
        }
        if (newExpedition.getRef() != null) {
            expedition.setRef(newExpedition.getRef());
        }

        // Mise à jour des informations géographiques
        if (newExpedition.getStartCountry() != null) {
            expedition.setStartCountry(newExpedition.getStartCountry());
        }
        if (newExpedition.getDestinationCountry() != null) {
            expedition.setDestinationCountry(newExpedition.getDestinationCountry());
        }

        // Mise à jour des dates
        if (newExpedition.getArrivalDate() != null) {
            expedition.setArrivalDate(newExpedition.getArrivalDate());
        }
        if (newExpedition.getStartDate() != null) {
            expedition.setStartDate(newExpedition.getStartDate());
        }

        // Mise à jour du client (Partners)
        if (newExpedition.getClientId() != null) {
            Optional<Partners> optionalClient = clientRepo.findById(newExpedition.getClientId());
            if (optionalClient.isPresent()) {
                expedition.setClient(optionalClient.get());
            } else {
                return "CLIENT_NOT_FOUND";
            }
        }

        // Mise à jour des métadonnées
        expedition.setEditedAt(LocalDateTime.now());
        expedition.setCreatedBy(user);

        packageRepository.save(expedition);
        return "SUCCESS";
    }


    public List<PackageDto> getPackagesByWarehouse(Long warehouseId) {
        List<Packages> packages = packageRepository.findByWarehouseId(warehouseId);

        return packages.stream()
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(Packages::getCreatedAt).reversed())
                .map(pkg -> {
                    PackageDto dto = new PackageDto();
                    dto.setId(pkg.getId());
                    dto.setRef(pkg.getRef());
                    dto.setWeight(pkg.getWeight());
                    dto.setWeight(pkg.getWeight());
                    dto.setCbn(pkg.getCbn());
                    dto.setStartDate(pkg.getStartDate());
                    dto.setArrivalDate(pkg.getArrivalDate());
                    dto.setStatus(pkg.getStatus().name());
                    dto.setDestinationCountry(pkg.getDestinationCountry());
                    dto.setExpeditionType(pkg.getExpeditionType());
                    dto.setItemQuantity(pkg.getItemQuantity());
                    dto.setStartCountry(pkg.getStartCountry());
                    dto.setWarehouseId(pkg.getWarehouse() != null ? pkg.getWarehouse().getId() : null);
                    dto.setWarehouseName(pkg.getWarehouse() != null ? pkg.getWarehouse().getName() : null);
                    dto.setWarehouseAddress(pkg.getWarehouse() != null ? pkg.getWarehouse().getAdresse() : null);
                    dto.setClientId(pkg.getClient() != null ? pkg.getClient().getId() : null);
                    dto.setClientName(pkg.getClient() != null
                            ? pkg.getClient().getFirstName() + " " + pkg.getClient().getLastName()
                            : null);
                    dto.setClientPhone(pkg.getClient() != null
                            ? pkg.getClient().getPhoneNumber()
                            : null);

                    return dto;
                })
                .collect(Collectors.toList());

    }

}
