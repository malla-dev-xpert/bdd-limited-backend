package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.PackageDto;
import com.xpertpro.bbd_project.dto.containers.ContainersDto;
import com.xpertpro.bbd_project.entity.*;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.ContainersRepository;
import com.xpertpro.bbd_project.repository.HarborRepository;
import com.xpertpro.bbd_project.repository.PartnerRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContainerServices {
    @Autowired
    ContainersRepository containersRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    HarborRepository harborRepository;
    @Autowired
    PartnerRepository partnerRepository;
    @Autowired
    LogServices logServices;


    @Transactional
    public String createContainer(ContainersDto containersDto, Long userId, Long supplierId) {
        // Validate reference uniqueness
        if (containersRepository.findByReference(containersDto.getReference()).isPresent()) {
            return "REF_EXIST";
        }

        // Get user (mandatory)
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec ID: " + userId));

        // Create new container
        Containers container = new Containers();
        container.setReference(containersDto.getReference());
        container.setIsAvailable(containersDto.getIsAvailable());
        container.setCreatedAt(LocalDateTime.now());
        container.setSize(containersDto.getSize());
        container.setLoadingFee(containersDto.getLoadingFee());
        container.setLocationFee(containersDto.getLocationFee());
        container.setMargin(containersDto.getMargin());
        container.setTelxFee(containersDto.getTelxFee());
        container.setOtherFees(containersDto.getOtherFees());
        container.setOverweightFee(containersDto.getOverweightFee());
        container.setLocalCharge(containersDto.getLocalCharge());
        container.setCheckingFee(containersDto.getCheckingFee());
        container.setIsTeam(false);

        // Calculer la somme totale de tous les frais + marge
        Double totalFees =
                (container.getLoadingFee() != null ? container.getLoadingFee() : 0.0) +
                        (container.getLocationFee() != null ? container.getLocationFee() : 0.0) +
                        (container.getTelxFee() != null ? container.getTelxFee() : 0.0) +
                        (container.getOtherFees() != null ? container.getOtherFees() : 0.0) +
                        (container.getOverweightFee() != null ? container.getOverweightFee() : 0.0) +
                        (container.getLocalCharge() != null ? container.getLocalCharge() : 0.0) +
                        (container.getCheckingFee() != null ? container.getCheckingFee() : 0.0) +
                        (container.getMargin() != null ? container.getMargin() : 0.0);

        container.setAmount(totalFees);
        container.setUser(user);
        container.setStatus(StatusEnum.PENDING); // Set default status

        // Handle optional supplier
        if (supplierId != null) {
            Partners supplier = partnerRepository.findById(supplierId)
                    .orElseThrow(() -> new EntityNotFoundException("Fournisseur non trouvé avec ID: " + supplierId));
            container.setSupplier(supplier);
        }

        // Save container
        Containers savedContainer = containersRepository.save(container);

        // Log action
        logServices.logAction(
                user,
                "AJOUTER_CONTENEUR",
                "Containers",
                savedContainer.getId());

        return "SUCCESS";
    }

    @Transactional
    public String updateContainer(Long id, ContainersDto containersDto, Long userId) {
        // Récupérer le conteneur existant
        Containers existingContainer = containersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conteneur non trouvé"));

        // Vérifier l'unicité de la référence (sauf pour le conteneur actuel)
        Optional<Containers> containerWithSameRef = containersRepository.findByReference(containersDto.getReference());
        if (containerWithSameRef.isPresent() && !containerWithSameRef.get().getId().equals(id)) {
            return "REF_EXIST";
        }

        // Vérifier l'existence de l'utilisateur
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ID: " + userId));

        // Mettre à jour les propriétés de base
        if (containersDto.getReference() != null) {
            existingContainer.setReference(containersDto.getReference());
        }
        if (containersDto.getIsTeam() != null) {
            existingContainer.setIsTeam(containersDto.getIsTeam());
        }
        if (containersDto.getSize() != null) {
            existingContainer.setSize(containersDto.getSize());
        }
        if (containersDto.getIsAvailable() != null) {
            existingContainer.setIsAvailable(containersDto.getIsAvailable());
        }
        existingContainer.setEditedAt(LocalDateTime.now());

        // Mettre à jour tous les frais
        if (containersDto.getLoadingFee() != null) {
            existingContainer.setLoadingFee(containersDto.getLoadingFee());
        }
        if (containersDto.getLocationFee() != null) {
            existingContainer.setLocationFee(containersDto.getLocationFee());
        }
        if (containersDto.getMargin() != null) {
            existingContainer.setMargin(containersDto.getMargin());
        }
        if (containersDto.getTelxFee() != null) {
            existingContainer.setTelxFee(containersDto.getTelxFee());
        }
        if (containersDto.getOtherFees() != null) {
            existingContainer.setOtherFees(containersDto.getOtherFees());
        }
        if (containersDto.getOverweightFee() != null) {
            existingContainer.setOverweightFee(containersDto.getOverweightFee());
        }
        if (containersDto.getLocalCharge() != null) {
            existingContainer.setLocalCharge(containersDto.getLocalCharge());
        }
        if (containersDto.getCheckingFee() != null) {
            existingContainer.setCheckingFee(containersDto.getCheckingFee());
        }

        // Recalculer le montant total
        Double totalFees =
                (existingContainer.getLoadingFee() != null ? existingContainer.getLoadingFee() : 0.0) +
                        (existingContainer.getLocationFee() != null ? existingContainer.getLocationFee() : 0.0) +
                        (existingContainer.getTelxFee() != null ? existingContainer.getTelxFee() : 0.0) +
                        (existingContainer.getOtherFees() != null ? existingContainer.getOtherFees() : 0.0) +
                        (existingContainer.getOverweightFee() != null ? existingContainer.getOverweightFee() : 0.0) +
                        (existingContainer.getLocalCharge() != null ? existingContainer.getLocalCharge() : 0.0) +
                        (existingContainer.getCheckingFee() != null ? existingContainer.getCheckingFee() : 0.0) +
                        (existingContainer.getMargin() != null ? existingContainer.getMargin() : 0.0);

        existingContainer.setAmount(totalFees);

        // Mettre à jour le fournisseur si nécessaire
        if (containersDto.getSupplier_id() != null) {
            Partners supplier = partnerRepository.findById(containersDto.getSupplier_id())
                    .orElseThrow(() -> new EntityNotFoundException("Fournisseur non trouvé avec ID: " + containersDto.getSupplier_id()));
            existingContainer.setSupplier(supplier);
        }

        // Sauvegarder les modifications
        containersRepository.save(existingContainer);

        // Logger l'action
        logServices.logAction(
                user,
                "MODIFIER_CONTENEUR",
                "Containers",
                existingContainer.getId());

        return "SUCCESS";
    }

    @Transactional
    public String deleteContainerById(Long containerId, Long userId) {
        Optional<Containers> optionalContainer = containersRepository.findById(containerId);
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (optionalContainer.isEmpty()) {
            return "CONTAINER_NOT_FOUND";
        }

        if (optionalUser.isEmpty()) {
            return "USER_NOT_FOUND";
        }

        Containers container = optionalContainer.get();

        if (container.getPackages() != null && !container.getPackages().isEmpty()) {
            return "PACKAGE_EXIST";
        }

        container.setStatus(StatusEnum.DELETE);
        container.setUser(optionalUser.get());
        containersRepository.save(container);
        return "DELETED";
    }

    public List<ContainersDto> getAllContainers(int page) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Containers> containers = containersRepository.findByStatusNot(StatusEnum.DELETE, pageable);

        return containers.stream()
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(Containers::getCreatedAt).reversed())
                .map(pkg -> {
                    ContainersDto dto = new ContainersDto();
                    // Informations de base
                    dto.setId(pkg.getId());
                    dto.setReference(pkg.getReference());
                    dto.setCreatedAt(pkg.getCreatedAt());
                    dto.setEditedAt(pkg.getEditedAt());
                    dto.setStatus(pkg.getStatus().name());
                    dto.setSize(pkg.getSize());
                    dto.setIsAvailable(pkg.getIsAvailable());
                    dto.setAmount(pkg.getAmount());
                    dto.setStartDeliveryDate(pkg.getStartDeliveryDate());
                    dto.setConfirmDeliveryDate(pkg.getConfirmDeliveryDate());
                    dto.setIsTeam(pkg.getIsTeam());

                    // Tous les frais
                    dto.setLoadingFee(pkg.getLoadingFee());
                    dto.setLocationFee(pkg.getLocationFee());
                    dto.setMargin(pkg.getMargin());
                    dto.setTelxFee(pkg.getTelxFee());
                    dto.setOtherFees(pkg.getOtherFees());
                    dto.setOverweightFee(pkg.getOverweightFee());
                    dto.setLocalCharge(pkg.getLocalCharge());
                    dto.setCheckingFee(pkg.getCheckingFee());

                    // Informations utilisateur
                    dto.setUserName(pkg.getUser() != null ?
                            pkg.getUser().getFirstName() + " " + pkg.getUser().getLastName() : null);
                    dto.setUserId(pkg.getUser() != null ? pkg.getUser().getId() : null);

                    // Informations fournisseur
                    dto.setSupplier_id(pkg.getSupplier() != null ? pkg.getSupplier().getId() : null);
                    dto.setSupplierPhone(pkg.getSupplier() != null ? pkg.getSupplier().getPhoneNumber() : null);
                    dto.setSupplierName(pkg.getSupplier() != null ?
                            pkg.getSupplier().getFirstName() + " " + pkg.getSupplier().getLastName() : null);

                    // Packages associés
                    List<PackageDto> packageResponseDtos = pkg.getPackages().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .filter(item -> item.getStatus() != StatusEnum.DELETE_ON_CONTAINER)
                            .map(packages -> {
                                PackageDto packageDto = new PackageDto();
                                packageDto.setId(packages.getId());
                                packageDto.setRef(packages.getRef());
                                packageDto.setWeight(packages.getWeight());
                                packageDto.setCbn(packages.getCbn());
                                packageDto.setStartDate(packages.getStartDate());
                                packageDto.setArrivalDate(packages.getArrivalDate());
                                packageDto.setStatus(packages.getStatus().name());
                                packageDto.setDestinationCountry(packages.getDestinationCountry());
                                packageDto.setExpeditionType(packages.getExpeditionType());
                                packageDto.setItemQuantity(packages.getItemQuantity());
                                packageDto.setStartCountry(packages.getStartCountry());
                                packageDto.setWarehouseId(packages.getWarehouse() != null ? packages.getWarehouse().getId() : null);
                                packageDto.setWarehouseName(packages.getWarehouse() != null ? packages.getWarehouse().getName() : null);
                                packageDto.setWarehouseAddress(packages.getWarehouse() != null ? packages.getWarehouse().getAdresse() : null);
                                packageDto.setClientId(packages.getClient() != null ? packages.getClient().getId() : null);
                                packageDto.setClientName(packages.getClient() != null
                                        ? packages.getClient().getFirstName() + " " + packages.getClient().getLastName()
                                        : null);
                                packageDto.setClientPhone(packages.getClient() != null
                                        ? packages.getClient().getPhoneNumber()
                                        : null);
                                return packageDto;
                            }).collect(Collectors.toList());

                    dto.setPackages(packageResponseDtos);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ContainersDto> getAllContainersNotInHarbor(int page) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Containers> containers = containersRepository.findByHarborIsNull(pageable);

        return containers.stream()
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(Containers::getCreatedAt).reversed())
                .map(pkg -> {
                    ContainersDto dto = new ContainersDto();
                    // Informations de base
                    dto.setId(pkg.getId());
                    dto.setReference(pkg.getReference());
                    dto.setCreatedAt(pkg.getCreatedAt());
                    dto.setEditedAt(pkg.getEditedAt());
                    dto.setStatus(pkg.getStatus().name());
                    dto.setSize(pkg.getSize());
                    dto.setIsAvailable(pkg.getIsAvailable());
                    dto.setAmount(pkg.getAmount());
                    dto.setStartDeliveryDate(pkg.getStartDeliveryDate());
                    dto.setConfirmDeliveryDate(pkg.getConfirmDeliveryDate());
                    dto.setIsTeam(pkg.getIsTeam());

                    // Tous les frais
                    dto.setLoadingFee(pkg.getLoadingFee());
                    dto.setLocationFee(pkg.getLocationFee());
                    dto.setMargin(pkg.getMargin());
                    dto.setTelxFee(pkg.getTelxFee());
                    dto.setOtherFees(pkg.getOtherFees());
                    dto.setOverweightFee(pkg.getOverweightFee());
                    dto.setLocalCharge(pkg.getLocalCharge());
                    dto.setCheckingFee(pkg.getCheckingFee());

                    // Informations utilisateur
                    dto.setUserName(pkg.getUser() != null ?
                            pkg.getUser().getFirstName() + " " + pkg.getUser().getLastName() : null);
                    dto.setUserId(pkg.getUser() != null ? pkg.getUser().getId() : null);

                    // Informations fournisseur
                    dto.setSupplier_id(pkg.getSupplier() != null ? pkg.getSupplier().getId() : null);
                    dto.setSupplierPhone(pkg.getSupplier() != null ? pkg.getSupplier().getPhoneNumber() : null);
                    dto.setSupplierName(pkg.getSupplier() != null ?
                            pkg.getSupplier().getFirstName() + " " + pkg.getSupplier().getLastName() : null);

                    // Packages associés
                    List<PackageDto> packageResponseDtos = pkg.getPackages().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .filter(item -> item.getStatus() != StatusEnum.DELETE_ON_CONTAINER)
                            .map(packages -> {
                                PackageDto packageDto = new PackageDto();
                                packageDto.setId(packages.getId());
                                packageDto.setRef(packages.getRef());
                                packageDto.setWeight(packages.getWeight());
                                packageDto.setCbn(packages.getCbn());
                                packageDto.setStartDate(packages.getStartDate());
                                packageDto.setArrivalDate(packages.getArrivalDate());
                                packageDto.setStatus(packages.getStatus().name());
                                packageDto.setDestinationCountry(packages.getDestinationCountry());
                                packageDto.setExpeditionType(packages.getExpeditionType());
                                packageDto.setItemQuantity(packages.getItemQuantity());
                                packageDto.setStartCountry(packages.getStartCountry());
                                packageDto.setWarehouseId(packages.getWarehouse() != null ? packages.getWarehouse().getId() : null);
                                packageDto.setWarehouseName(packages.getWarehouse() != null ? packages.getWarehouse().getName() : null);
                                packageDto.setWarehouseAddress(packages.getWarehouse() != null ? packages.getWarehouse().getAdresse() : null);
                                packageDto.setClientId(packages.getClient() != null ? packages.getClient().getId() : null);
                                packageDto.setClientName(packages.getClient() != null
                                        ? packages.getClient().getFirstName() + " " + packages.getClient().getLastName()
                                        : null);
                                packageDto.setClientPhone(packages.getClient() != null
                                        ? packages.getClient().getPhoneNumber()
                                        : null);
                                return packageDto;
                            }).collect(Collectors.toList());

                    dto.setPackages(packageResponseDtos);

                    return dto;
                })
                .collect(Collectors.toList());

    }

    public String retrieveContainerToHarbor(Long containerId, Long userId, Long harborId) {

        // Récupération des entités avec gestion d'erreur améliorée
        Containers container = containersRepository.findById(containerId)
                .orElseThrow(() -> new EntityNotFoundException("Conteneur non trouvé avec ID: " + containerId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec ID: " + userId));

        Harbor harbor = harborRepository.findById(harborId)
                .orElseThrow(() -> new EntityNotFoundException("Port non trouvé avec ID: " + harborId));

        // Vérification si le conteneur contient des colis non supprimés
        if (hasActivePackages(container)) {
            throw new IllegalStateException("Impossible de retirer le conteneur: il contient encore des colis actifs");
        }

        // Vérification du statut actuel
        if (container.getStatus() == StatusEnum.RETRIEVE) {
            throw new IllegalStateException("Le conteneur a déjà été retiré");
        }

        // Mise à jour du conteneur
        container.setStatus(StatusEnum.RETRIEVE);
        container.setUser(user);
        container.setHarbor(harbor);
        container.setEditedAt(LocalDateTime.now());

        containersRepository.save(container);

        return "Conteneur retiré avec succès";
    }

    private boolean hasActivePackages(Containers container) {
        return container.getPackages().stream()
                .anyMatch(p -> p.getStatus() != StatusEnum.DELETE && p.getStatus() != StatusEnum.DELETE_ON_CONTAINER);
    }

    @Transactional()
    public ContainersDto getContainerById(Long containerId) {
        Containers pkg = containersRepository.findById(containerId)
                .orElseThrow(() -> new RuntimeException("Conteneur non trouvé avec l'ID: " + containerId));

        if (pkg.getStatus() == StatusEnum.DELETE) {
            throw new RuntimeException("Ce conteneur est supprimé");
        }

        ContainersDto dto = new ContainersDto();
        // Informations de base
        dto.setId(pkg.getId());
        dto.setReference(pkg.getReference());
        dto.setCreatedAt(pkg.getCreatedAt());
        dto.setEditedAt(pkg.getEditedAt());
        dto.setStatus(pkg.getStatus().name());
        dto.setSize(pkg.getSize());
        dto.setIsAvailable(pkg.getIsAvailable());
        dto.setAmount(pkg.getAmount());
        dto.setStartDeliveryDate(pkg.getStartDeliveryDate());
        dto.setConfirmDeliveryDate(pkg.getConfirmDeliveryDate());
        dto.setIsTeam(pkg.getIsTeam());

        // Tous les frais
        dto.setLoadingFee(pkg.getLoadingFee());
        dto.setLocationFee(pkg.getLocationFee());
        dto.setMargin(pkg.getMargin());
        dto.setTelxFee(pkg.getTelxFee());
        dto.setOtherFees(pkg.getOtherFees());
        dto.setOverweightFee(pkg.getOverweightFee());
        dto.setLocalCharge(pkg.getLocalCharge());
        dto.setCheckingFee(pkg.getCheckingFee());

        // Informations utilisateur
        dto.setUserName(pkg.getUser() != null ?
                pkg.getUser().getFirstName() + " " + pkg.getUser().getLastName() : null);
        dto.setUserId(pkg.getUser() != null ? pkg.getUser().getId() : null);

        // Informations fournisseur
        dto.setSupplier_id(pkg.getSupplier() != null ? pkg.getSupplier().getId() : null);
        dto.setSupplierPhone(pkg.getSupplier() != null ? pkg.getSupplier().getPhoneNumber() : null);
        dto.setSupplierName(pkg.getSupplier() != null ?
                pkg.getSupplier().getFirstName() + " " + pkg.getSupplier().getLastName() : null);

        List<PackageDto> packageResponseDtos = pkg.getPackages().stream()
                .filter(pkgs -> pkgs.getStatus() != StatusEnum.DELETE)
                .filter(pkgs -> pkgs.getStatus() != StatusEnum.DELETE_ON_CONTAINER)
                .map(packages -> {
                    PackageDto packageDto = new PackageDto();
                    packageDto.setId(packages.getId());
                    packageDto.setRef(packages.getRef());
                    packageDto.setWeight(packages.getWeight());
                    packageDto.setWeight(packages.getWeight());
                    packageDto.setCbn(packages.getCbn());
                    packageDto.setStartDate(packages.getStartDate());
                    packageDto.setArrivalDate(packages.getArrivalDate());
                    packageDto.setStatus(packages.getStatus().name());
                    packageDto.setDestinationCountry(packages.getDestinationCountry());
                    packageDto.setExpeditionType(packages.getExpeditionType());
                    packageDto.setItemQuantity(packages.getItemQuantity());
                    packageDto.setStartCountry(packages.getStartCountry());
                    packageDto.setWarehouseId(packages.getWarehouse() != null ? packages.getWarehouse().getId() : null);
                    packageDto.setWarehouseName(packages.getWarehouse() != null ? packages.getWarehouse().getName() : null);
                    packageDto.setWarehouseAddress(packages.getWarehouse() != null ? packages.getWarehouse().getAdresse() : null);
                    packageDto.setClientId(packages.getClient() != null ? packages.getClient().getId() : null);
                    packageDto.setClientName(packages.getClient() != null
                            ? packages.getClient().getFirstName() + " " + packages.getClient().getLastName()
                            : null);
                    packageDto.setClientPhone(packages.getClient() != null
                            ? packages.getClient().getPhoneNumber()
                            : null);
                    return packageDto;
                }).collect(Collectors.toList());

        dto.setPackages(packageResponseDtos);

        return dto;
    }

    @Transactional
    public String startDelivery(Long id, Long userId, LocalDateTime startDeliveryDate) {
        Containers containers = containersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Container not found with ID: " + id));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        if(containers.getPackages().isEmpty()){
            return "NO_PACKAGE_FOR_DELIVERY";
        }

        // Mettre à jour le statut de tous les packages du conteneur
        containers.getPackages().forEach(pkg -> {
            pkg.setStatus(StatusEnum.INPROGRESS);
            pkg.setEditedAt(LocalDateTime.now());
        });

        containers.setStatus(StatusEnum.INPROGRESS);
        containers.setUser(user);
        containers.setEditedAt(LocalDateTime.now());
        containers.setStartDeliveryDate(startDeliveryDate != null ? startDeliveryDate : LocalDateTime.now());
        containersRepository.save(containers);

        // Log action
        logServices.logAction(
                user,
                "DEMARER_LA_LIVRAISON",
                "Containers",
                containers.getId());
        return "La livraison démarre avec succès.";
    }

    @Transactional
    public String confirmDelivery(Long id, Long userId, LocalDateTime confirmDeliveryDate) {
        Containers containers = containersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Container not found with ID: " + id));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        if(containers.getPackages().isEmpty()){
            return "NO_PACKAGE_FOR_DELIVERY";
        }

        if(containers.getStatus() != StatusEnum.INPROGRESS){
            return "CONTAINER_NOT_IN_PROGRESS";
        }

        // Mettre à jour le statut de tous les packages du conteneur
        containers.getPackages().forEach(pkg -> {
            pkg.setStatus(StatusEnum.DELIVERED);
            pkg.setEditedAt(LocalDateTime.now());
        });

        containers.setStatus(StatusEnum.RECEIVED);
        containers.setUser(user);
        containers.setEditedAt(LocalDateTime.now());
        containers.setConfirmDeliveryDate(confirmDeliveryDate != null ? confirmDeliveryDate : LocalDateTime.now());
        containersRepository.save(containers);

        // Log action
        logServices.logAction(
                user,
                "CONFIRMER_LA_RECEPTION",
                "Containers",
                containers.getId());
        return "Le conteneur est arriver avec succès.";
    }
}
