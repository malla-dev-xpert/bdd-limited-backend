package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.Package.PackageCreateDto;
import com.xpertpro.bbd_project.dto.Package.PackageResponseDto;
import com.xpertpro.bbd_project.dtoMapper.PackageDtoMapper;
import com.xpertpro.bbd_project.entity.Packages;
import com.xpertpro.bbd_project.entity.Payments;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.entity.Warehouse;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.PackageRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import com.xpertpro.bbd_project.repository.WarehouseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public String createPackage(Long warehouseId, PackageCreateDto dto, Long userId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new EntityNotFoundException("Entrepôt non trouvé"));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        if(packageRepository.findByReference(dto.getReference()).isPresent()){
            return "DUPLICATE_REFERENCE";
        }

        Packages pkg = packageDtoMapper.toEntity(dto);
        pkg.setWeight(dto.getWeight());
        pkg.setDimensions(dto.getDimensions());
        pkg.setReference(dto.getReference());
        pkg.setCreatedAt(dto.getCreatedAt());
        pkg.setStatus(StatusEnum.CREATE);
        pkg.setWarehouse(warehouse);
        pkg.setUser(user);

        packageRepository.save(pkg);
        return "SUCCESS";
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

}
