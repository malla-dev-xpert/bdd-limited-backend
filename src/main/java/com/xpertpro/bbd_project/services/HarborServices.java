package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.PackageDto;
import com.xpertpro.bbd_project.dto.containers.ContainersDto;
import com.xpertpro.bbd_project.dto.harbor.HarborDto;
import com.xpertpro.bbd_project.dto.items.ItemDto;
import com.xpertpro.bbd_project.entity.Harbor;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.dtoMapper.HarborDtoMapper;
import com.xpertpro.bbd_project.repository.HarborRepository;
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
public class HarborServices {
    @Autowired
    HarborRepository harborRepository;
    @Autowired
    HarborDtoMapper harborDtoMapper;
    @Autowired
    UserRepository userRepository;

    public String createHarbor(HarborDto harborDto, Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (harborRepository.findByName(harborDto.getName()).isPresent()) {
            return "NAME_EXIST";
        }

        Harbor harbor = harborDtoMapper.toEntity(harborDto);

        harbor.setCreatedAt(harborDto.getCreatedAt());
        harbor.setUser(user);
        harborRepository.save(harbor);
        return "SUCCESS";
    }

    public String updateHarbor(Long id, HarborDto newHarbor, Long userId) {
        Optional<Harbor> optionalHarbor = harborRepository.findById(id);
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (harborRepository.findByName(newHarbor.getName()).isPresent()) {
            return "NAME_EXIST";
        }

        if (optionalHarbor.isPresent() && optionalUser.isPresent()) {
            Harbor harbor = optionalHarbor.get();

            if (newHarbor.getLocation() != null) harbor.setLocation(newHarbor.getLocation());
            if (newHarbor.getName() != null) harbor.setName(newHarbor.getName());

            harbor.setEditedAt(newHarbor.getEditedAt());
            harbor.setUser(optionalUser.get());

            harborRepository.save(harbor);
            return "SUCCESS";
        } else {
            throw new RuntimeException("Warehouse not found with ID: " + id);
        }
    }

    public String deleteHarbor(Long id, Long userId) {
        Harbor harbor = harborRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Harbor not found with ID: " + id));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        harbor.setStatus(StatusEnum.DELETE);
        harbor.setUser(user);
        harborRepository.save(harbor);
        return "Harbor deleted successfully";
    }

    public String disableHarbor(Long id, Long userId) {
        Harbor harbor = harborRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Harbor not found with ID: " + id));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        harbor.setStatus(StatusEnum.DISABLE);
        harbor.setUser(user);
        harborRepository.save(harbor);
        return "Harbor disable successfully";
    }

    public List<HarborDto> getAllHarbor(int page, String query) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Harbor> harbors = harborRepository.findByStatus(StatusEnum.CREATE, pageable);

        if (query != null && !query.isEmpty()) {
            harbors = harborRepository.findByStatusAndSearchQuery(
                    StatusEnum.CREATE,
                    "%" + query.toLowerCase() + "%",
                    pageable
            );
        } else {
            harbors = harborRepository.findByStatus(StatusEnum.CREATE, pageable);
        }


        return harbors.stream()
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(Harbor::getCreatedAt).reversed())
                .map(pkg -> {
                    HarborDto dto = new HarborDto();
                    dto.setId(pkg.getId());
                    dto.setName(pkg.getName());
                    dto.setLocation(pkg.getLocation());
                    dto.setCreatedAt(pkg.getCreatedAt());
                    dto.setUserid(pkg.getUser().getId());
                    dto.setUserName(pkg.getUser() != null
                            ? pkg.getUser().getFirstName() + " " + pkg.getUser().getLastName()
                            : null);
                    dto.setEditedAt(pkg.getEditedAt());

                    List<ContainersDto> containersDtos = pkg.getContainers().stream().map(item -> {
                        ContainersDto containersDto = new ContainersDto();
                        containersDto.setReference(item.getReference());
                        containersDto.setId(item.getId());
                        containersDto.setSize(item.getSize());
                        containersDto.setIsAvailable(item.getIsAvailable());
                        containersDto.setStatus(item.getStatus().name());
                        containersDto.setSupplier_id(item.getSupplier() != null ? item.getSupplier().getId() : null);
                        containersDto.setSupplierPhone(item.getSupplier() != null ? item.getSupplier().getPhoneNumber() : null);
                        containersDto.setSupplierName(item.getSupplier() != null ? item.getSupplier().getFirstName()
                                + " " + item.getSupplier().getLastName(): null);

                        List<PackageDto> packageResponseDtos = item.getPackages().stream()
                                .filter(pkgItem -> pkgItem.getStatus() != StatusEnum.DELETE)
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

                        containersDto.setPackages(packageResponseDtos);

                        return containersDto;
                    }).collect(Collectors.toList());

                    dto.setContainers(containersDtos);

                    return dto;
                })
                .collect(Collectors.toList());

    }

    public HarborDto getHarborById(Long id) {
        // Trouver le harbor par ID avec le statut CREATE
        Harbor harbor = harborRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Harbor not found with id: " + id));

        // Convertir en DTO
        HarborDto dto = new HarborDto();
        dto.setId(harbor.getId());
        dto.setName(harbor.getName());
        dto.setLocation(harbor.getLocation());
        dto.setCreatedAt(harbor.getCreatedAt());
        dto.setUserid(harbor.getUser().getId());
        dto.setUserName(harbor.getUser() != null
                ? harbor.getUser().getFirstName() + " " + harbor.getUser().getLastName()
                : null);
        dto.setEditedAt(harbor.getEditedAt());

        // Convertir les containers associés
        List<ContainersDto> containersDtos = harbor.getContainers().stream()
                .filter(container -> container.getStatus() != StatusEnum.DELETE)
                .map(item -> {
                    ContainersDto containersDto = new ContainersDto();
                    containersDto.setReference(item.getReference());
                    containersDto.setIsAvailable(item.getIsAvailable());
                    containersDto.setSize(item.getSize());
                    containersDto.setStatus(item.getStatus().name());
                    containersDto.setSupplier_id(item.getSupplier() != null ? item.getSupplier().getId() : null);
                    containersDto.setSupplierPhone(item.getSupplier() != null ? item.getSupplier().getPhoneNumber() : null);
                    containersDto.setSupplierName(item.getSupplier() != null ? item.getSupplier().getFirstName()
                            + " " + item.getSupplier().getLastName(): null);

                    List<PackageDto> packageResponseDtos = item.getPackages().stream()
                            .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
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

                    containersDto.setPackages(packageResponseDtos);

                    return containersDto;
                }).collect(Collectors.toList());

        dto.setContainers(containersDtos);

        return dto;
    }
}
