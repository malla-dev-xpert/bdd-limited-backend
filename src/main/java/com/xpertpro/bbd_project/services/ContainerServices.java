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

    public String updateContainer(Long id, ContainersDto containersDto, Long userId) {
        Containers newContainer = containersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conteneur non trouvé"));
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        Optional<Containers> existingContainer = containersRepository.findByReference(containersDto.getReference());
        if (existingContainer.isPresent() && !existingContainer.get().getId().equals(id)) {
            return "REF_EXIST";
        }

        if (optionalUser.isPresent()) {
            if (containersDto.getReference() != null) newContainer.setReference(containersDto.getReference());
            if (containersDto.getSize() != null) newContainer.setSize(containersDto.getSize());
            if (containersDto.getIsAvailable() != null) newContainer.setIsAvailable(containersDto.getIsAvailable());
            newContainer.setEditedAt(containersDto.getEditedAt());

            containersRepository.save(newContainer);
            return "SUCCESS";
        } else {
            throw new RuntimeException("User not found with ID: " + id);
        }
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
                    dto.setId(pkg.getId());
                    dto.setReference(pkg.getReference());
                    dto.setCreatedAt(pkg.getCreatedAt());
                    dto.setEditedAt(pkg.getEditedAt());
                    dto.setStatus(pkg.getStatus().name());
                    dto.setSize(pkg.getSize());
                    dto.setStatus(pkg.getStatus().name());
                    dto.setIsAvailable(pkg.getIsAvailable());
                    dto.setUserName(pkg.getUser() != null ? pkg.getUser().getFirstName() + " " + pkg.getUser().getLastName() : null);
                    dto.setUserId(pkg.getUser() != null ? pkg.getUser().getId() : null);
                    dto.setSupplier_id(pkg.getSupplier() != null ? pkg.getSupplier().getId() : null);
                    dto.setSupplierPhone(pkg.getSupplier() != null ? pkg.getSupplier().getPhoneNumber() : null);
                    dto.setSupplierName(pkg.getSupplier() != null ? pkg.getSupplier().getFirstName() + " " +  pkg.getSupplier().getLastName(): null);
//                    dto.setHarborId(pkg.getHarbor() != null ? pkg.getHarbor().getId() : null);
//                    dto.setHarborName(pkg.getHarbor() != null ? pkg.getHarbor().getName() : null);

                    List<PackageDto> packageResponseDtos = pkg.getPackages().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .filter(item -> item.getStatus() != StatusEnum.DELETE_ON_CONTAINER)
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
                    dto.setId(pkg.getId());
                    dto.setReference(pkg.getReference());
                    dto.setCreatedAt(pkg.getCreatedAt() != null ? pkg.getCreatedAt() : null);
                    dto.setEditedAt(pkg.getEditedAt() != null ? pkg.getCreatedAt() : null);
                    dto.setStatus(pkg.getStatus().name());
                    dto.setSize(pkg.getSize());
                    dto.setStatus(pkg.getStatus().name());
                    dto.setIsAvailable(pkg.getIsAvailable());
                    dto.setUserName(pkg.getUser() != null ? pkg.getUser().getFirstName() + " " + pkg.getUser().getLastName() : null);
                    dto.setUserId(pkg.getUser() != null ? pkg.getUser().getId() : null);
                    dto.setHarborId(pkg.getHarbor() != null ? pkg.getHarbor().getId() : null);
                    dto.setHarborName(pkg.getHarbor() != null ? pkg.getHarbor().getName() : null);
                    dto.setSupplier_id(pkg.getSupplier() != null ? pkg.getSupplier().getId() : null);
                    dto.setSupplierPhone(pkg.getSupplier() != null ? pkg.getSupplier().getPhoneNumber() : null);
                    dto.setSupplierName(pkg.getSupplier() != null ? pkg.getSupplier().getFirstName() + " " +  pkg.getSupplier().getLastName(): null);

                    List<PackageDto> packageResponseDtos = pkg.getPackages().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .filter(item -> item.getStatus() != StatusEnum.DELETE_ON_CONTAINER)
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
        Containers container = containersRepository.findById(containerId)
                .orElseThrow(() -> new RuntimeException("Conteneur non trouvé avec l'ID: " + containerId));

        if (container.getStatus() == StatusEnum.DELETE) {
            throw new RuntimeException("Ce conteneur est supprimé");
        }

        ContainersDto dto = new ContainersDto();
        dto.setId(container.getId());
        dto.setReference(container.getReference());
        dto.setCreatedAt(container.getCreatedAt());
        dto.setSize(container.getSize());
        dto.setEditedAt(container.getEditedAt());
        dto.setStatus(container.getStatus().name());
        dto.setIsAvailable(container.getIsAvailable());
        dto.setUserName(container.getUser() != null
                ? container.getUser().getFirstName() + " " + container.getUser().getLastName()
                : null);
        dto.setUserId(container.getUser() != null ? container.getUser().getId() : null);
        dto.setSupplier_id(container.getSupplier() != null ? container.getSupplier().getId() : null);
        dto.setSupplierPhone(container.getSupplier() != null ? container.getSupplier().getPhoneNumber() : null);
        dto.setSupplierName(container.getSupplier() != null ? container.getSupplier().getFirstName() + " " +  container.getSupplier().getLastName(): null);

        List<PackageDto> packageResponseDtos = container.getPackages().stream()
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE_ON_CONTAINER)
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

    public String startDelivery(Long id, Long userId) {
        Containers containers = containersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Container not found with ID: " + id));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        if(containers.getPackages().isEmpty()){
            return "NO_PACKAGE_FOR_DELIVERY";
        }

        containers.setStatus(StatusEnum.INPROGRESS);
        containers.setUser(user);
        containers.setEditedAt(LocalDateTime.now());
        containersRepository.save(containers);

        // Log action
        logServices.logAction(
                user,
                "DEMARER_LA_LIVRAISON",
                "Containers",
                containers.getId());
        return "La livraison démarre avec succès.";
    }

    public String confirmDelivery(Long id, Long userId) {
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

        containers.setStatus(StatusEnum.RECEIVED);
        containers.setUser(user);
        containers.setEditedAt(LocalDateTime.now());
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
