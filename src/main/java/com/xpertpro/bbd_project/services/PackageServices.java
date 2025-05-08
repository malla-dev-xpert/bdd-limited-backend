package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.items.ItemDto;
import com.xpertpro.bbd_project.dto.Package.PackageCreateDto;
import com.xpertpro.bbd_project.dto.Package.PackageResponseDto;
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
import java.util.stream.Collectors;

@Service
public class PackageServices {
    @Autowired
    PackageRepository packageRepository;
    @Autowired
    WarehouseRepository warehouseRepository;
    @Autowired
    PackageDtoMapper packageDtoMapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PartnerRepository partnerRepository;
    @Autowired
    ItemsRepository itemsRepository;
    @Autowired
    ContainersRepository containersRepository;

    @Transactional
    public Packages createPackageWithItems(Long warehouseId,
                                           Long userId,
                                           Long partnerId,
                                           PackageCreateDto dto) {

        if (packageRepository.findByReference(dto.getReference()).isPresent()) {
            throw new RuntimeException("DUPLICATE_REFERENCE");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Partners partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partenaire introuvable"));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Entrepôt introuvable"));

        // Création du colis
        Packages newPackage = new Packages();
        newPackage.setReference(dto.getReference());
        newPackage.setWeight(dto.getWeight());
        newPackage.setDimensions(dto.getDimensions());
        newPackage.setCreatedAt(dto.getCreatedAt());
        newPackage.setUser(user);
        newPackage.setPartner(partner);
        newPackage.setWarehouse(warehouse);
        newPackage.setStatus(StatusEnum.PENDING);

        packageRepository.save(newPackage);

        return newPackage;
    }

    public List<PackageResponseDto> listUnassignedPackages(Long warehouseId) {
        List<Packages> packages = packageRepository.findByWarehouseIdAndContainerIsNull(warehouseId);

        return packages.stream()
                .map(pkg -> {
                    PackageResponseDto dto = new PackageResponseDto();
                    dto.setId(pkg.getId());
                    dto.setReference(pkg.getReference());
                    dto.setWeight(pkg.getWeight());
                    dto.setDimensions(pkg.getDimensions());
                    dto.setCreatedAt(pkg.getCreatedAt());
                    dto.setCreatedAt(pkg.getCreatedAt());
                    dto.setWarehouseId(pkg.getWarehouse() != null ? pkg.getWarehouse().getId() : null);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<PackageResponseDto> getPackagesByWarehouse(Long warehouseId) {
        List<Packages> packages = packageRepository.findByWarehouseId(warehouseId);

            return packages.stream()
                    .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                    .sorted(Comparator.comparing(Packages::getCreatedAt).reversed())
                    .map(pkg -> {
                        PackageResponseDto dto = new PackageResponseDto();
                        dto.setId(pkg.getId());
                        dto.setReference(pkg.getReference());
                        dto.setWeight(pkg.getWeight());
                        dto.setDimensions(pkg.getDimensions());
                        dto.setCreatedAt(pkg.getCreatedAt());
                        dto.setEditedAt(pkg.getEditedAt());
                        dto.setStatus(pkg.getStatus().name());
                        dto.setWarehouseId(pkg.getWarehouse() != null ? pkg.getWarehouse().getId() : null);
                        dto.setUserId(pkg.getUser() != null ? pkg.getUser().getId() : null);
                        dto.setPartnerId(pkg.getPartner() != null ? pkg.getPartner().getId() : null);
                        dto.setPartnerName(pkg.getPartner() != null
                                ? pkg.getPartner().getFirstName() + " " + pkg.getPartner().getLastName()
                                : null);
                        dto.setPartnerPhoneNumber(pkg.getPartner() != null
                                ? pkg.getPartner().getPhoneNumber()
                                : null);

                        List<ItemDto> itemDtos = pkg.getItems().stream()
                                .filter(item -> item.getStatus() != StatusEnum.DELETE)
                                .map(item -> {
                            ItemDto itemDto = new ItemDto();
                            itemDto.setId(item.getId());
                            itemDto.setDescription(item.getDescription());
                            itemDto.setQuantity(item.getQuantity());
                            itemDto.setUnitPrice(item.getUnitPrice());
                            return itemDto;
                        }).collect(Collectors.toList());

                        dto.setItems(itemDtos);

                        return dto;
                    })
                    .collect(Collectors.toList());

    }

    public List<PackageResponseDto> getAllPackages(int page) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Packages> packages = packageRepository.findByStatusNot(StatusEnum.DELETE, pageable);

        return packages.stream()
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(Packages::getCreatedAt).reversed())
                .map(pkg -> {
                    PackageResponseDto dto = new PackageResponseDto();
                    dto.setId(pkg.getId());
                    dto.setReference(pkg.getReference());
                    dto.setWeight(pkg.getWeight());
                    dto.setDimensions(pkg.getDimensions());
                    dto.setCreatedAt(pkg.getCreatedAt());
                    dto.setEditedAt(pkg.getEditedAt());
                    dto.setStatus(pkg.getStatus().name());
                    dto.setWarehouseId(pkg.getWarehouse() != null ? pkg.getWarehouse().getId() : null);
                    dto.setWarehouseName(pkg.getWarehouse() != null ? pkg.getWarehouse().getName() : null);
                    dto.setWarehouseAddress(pkg.getWarehouse() != null ? pkg.getWarehouse().getAdresse() : null);
                    dto.setUserId(pkg.getUser() != null ? pkg.getUser().getId() : null);
                    dto.setPartnerId(pkg.getPartner() != null ? pkg.getPartner().getId() : null);
                    dto.setPartnerName(pkg.getPartner() != null
                            ? pkg.getPartner().getFirstName() + " " + pkg.getPartner().getLastName()
                            : null);
                    dto.setPartnerPhoneNumber(pkg.getPartner() != null
                            ? pkg.getPartner().getPhoneNumber()
                            : null);

                    List<ItemDto> itemDtos = pkg.getItems().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .map(item -> {
                        ItemDto itemDto = new ItemDto();
                        itemDto.setId(item.getId());
                        itemDto.setDescription(item.getDescription());
                        itemDto.setQuantity(item.getQuantity());
                        itemDto.setUnitPrice(item.getUnitPrice());
                        return itemDto;
                    }).collect(Collectors.toList());

                    dto.setItems(itemDtos);

                    return dto;
                })
                .collect(Collectors.toList());

    }

    public List<PackageResponseDto> getAllPackagesReceived(int page) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Packages> packages = packageRepository.findByStatusNot(StatusEnum.DELETE, pageable);

        return packages.stream()
                .filter(pkg -> pkg.getStatus() == StatusEnum.RECEIVED)
                .sorted(Comparator.comparing(Packages::getCreatedAt).reversed())
                .map(pkg -> {
                    PackageResponseDto dto = new PackageResponseDto();
                    dto.setId(pkg.getId());
                    dto.setReference(pkg.getReference());
                    dto.setWeight(pkg.getWeight());
                    dto.setDimensions(pkg.getDimensions());
                    dto.setCreatedAt(pkg.getCreatedAt());
                    dto.setEditedAt(pkg.getEditedAt());
                    dto.setStatus(pkg.getStatus().name());
                    dto.setWarehouseId(pkg.getWarehouse() != null ? pkg.getWarehouse().getId() : null);
                    dto.setWarehouseName(pkg.getWarehouse() != null ? pkg.getWarehouse().getName() : null);
                    dto.setWarehouseAddress(pkg.getWarehouse() != null ? pkg.getWarehouse().getAdresse() : null);
                    dto.setUserId(pkg.getUser() != null ? pkg.getUser().getId() : null);
                    dto.setPartnerId(pkg.getPartner() != null ? pkg.getPartner().getId() : null);
                    dto.setPartnerName(pkg.getPartner() != null
                            ? pkg.getPartner().getFirstName() + " " + pkg.getPartner().getLastName()
                            : null);
                    dto.setPartnerPhoneNumber(pkg.getPartner() != null
                            ? pkg.getPartner().getPhoneNumber()
                            : null);

                    List<ItemDto> itemDtos = pkg.getItems().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .map(item -> {
                                ItemDto itemDto = new ItemDto();
                                itemDto.setId(item.getId());
                                itemDto.setDescription(item.getDescription());
                                itemDto.setQuantity(item.getQuantity());
                                itemDto.setUnitPrice(item.getUnitPrice());
                                return itemDto;
                            }).collect(Collectors.toList());

                    dto.setItems(itemDtos);

                    return dto;
                })
                .collect(Collectors.toList());

    }

    public String addItemsToPackage(Long packageId, List<ItemDto> items, Long userId) {
        Packages pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Colis introuvable"));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        for (ItemDto dto : items) {
            Items item = new Items();
            item.setDescription(dto.getDescription());
            item.setQuantity(dto.getQuantity());
            item.setUnitPrice(dto.getUnitPrice());
            item.setPackages(pkg);
            item.setStatus(StatusEnum.CREATE);
            item.setUser(user);
            pkg.getItems().add(item);
        }
        packageRepository.save(pkg);

        return "SUCCESS";
    }

    public String receivePackages(Long id, Long userId, Long warehouseId) {
        Packages packages = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with ID: " + id));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                        .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + id));

        packages.setStatus(StatusEnum.RECEIVED);
        packages.setUser(user);
        packages.setWarehouse(warehouse);
        packageRepository.save(packages);
        return "Package Received successfully";
    }

    public String removePackageFromContainer(Long packageId, Long userId, Long containerId) {
        Packages packages = packageRepository.findById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found with ID: " + packageId));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Containers container = containersRepository.findById(containerId)
                .orElseThrow(() -> new RuntimeException("Container not found with ID: " + containerId));

        if (!container.equals(packages.getContainer())) {
            return "PACKAGE_NOT_IN_SPECIFIED_CONTAINER";
        }

        if (container.getStatus() != StatusEnum.PENDING) {
            return "CONTAINER_NOT_EDITABLE";
        }

        packages.setContainer(null);
        packages.setStatus(StatusEnum.RECEIVED);
        packages.setUser(user);
        packages.setEditedAt(LocalDateTime.now());

        packageRepository.save(packages);
        return "PACKAGE_REMOVED_FROM_CONTAINER";
    }


    public String deletePackages(Long id, Long userId) {
        Packages packages = packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Package not found with ID: " + id));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        packages.setStatus(StatusEnum.DELETE);
        packages.setUser(user);
        packageRepository.save(packages);
        return "Package deleted successfully";
    }

    public String updatePackage(Long id, Long userId, PackageResponseDto dto) {
        Packages existingPackage = packageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Package not found with id: " + id));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        Partners partner = partnerRepository.findById(dto.getPartnerId())
                .orElseThrow(() -> new EntityNotFoundException("Partner not found"));
        if (packageRepository.findByReference(dto.getReference()).isPresent()) {
            return "DUPLICATE_REFERENCE";
        }

        existingPackage.setReference(dto.getReference());
        existingPackage.setWeight(dto.getWeight());
        existingPackage.setDimensions(dto.getDimensions());
        existingPackage.setEditedAt(LocalDateTime.now());
        existingPackage.setWarehouse(warehouse);
        existingPackage.setPartner(partner);
        existingPackage.setUser(user);

        packageRepository.save(existingPackage);

        return "Package updated";
    }
}
