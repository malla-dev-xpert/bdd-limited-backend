package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.Package.PackageResponseDto;
import com.xpertpro.bbd_project.dto.containers.ContainersDto;
import com.xpertpro.bbd_project.entity.*;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.ContainersRepository;
import com.xpertpro.bbd_project.repository.HarborRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import jakarta.transaction.Transactional;
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
public class ContainerServices {
    @Autowired
    ContainersRepository containersRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    HarborRepository harborRepository;


    public String createContainer(ContainersDto containersDto, Long userId) {
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (containersRepository.findByReference(containersDto.getReference()).isPresent()) {
            return "REF_EXIST";
        }

        if(optionalUser.isPresent()){
            Containers containers = new Containers();

            containers.setReference(containersDto.getReference());
            containers.setIsAvailable(containersDto.getIsAvailable());
            containers.setCreatedAt(containersDto.getCreatedAt());
            containers.setUser(optionalUser.get());

            containersRepository.save(containers);
            return "SUCCESS";
        }else{
            throw new RuntimeException("User not found with ID: " + userId);
        }

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
                    dto.setStatus(pkg.getStatus().name());
                    dto.setIsAvailable(pkg.getIsAvailable());
                    dto.setUserName(pkg.getUser() != null ? pkg.getUser().getFirstName() + " " + pkg.getUser().getLastName() : null);
                    dto.setUserId(pkg.getUser() != null ? pkg.getUser().getId() : null);
//                    dto.setHarborId(pkg.getHarbor() != null ? pkg.getHarbor().getId() : null);
//                    dto.setHarborName(pkg.getHarbor() != null ? pkg.getHarbor().getName() : null);

                    List<PackageResponseDto> packageResponseDtos = pkg.getPackages().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .filter(item -> item.getStatus() != StatusEnum.DELETE_ON_CONTAINER)
                            .map(packages -> {
                                PackageResponseDto packageDto = new PackageResponseDto();
                                packageDto.setId(packages.getId());
                                packageDto.setReference(packages.getReference());
                                packageDto.setPartnerName(packages.getPartner().getFirstName() + " " + packages.getPartner().getLastName());
                                packageDto.setPartnerPhoneNumber(packages.getPartner().getPhoneNumber());
                                packageDto.setWarehouseName(packages.getWarehouse().getName());
                                packageDto.setWarehouseAddress(packages.getWarehouse().getAdresse());
                                return packageDto;
                            }).collect(Collectors.toList());

                    dto.setPackages(packageResponseDtos);

                    return dto;
                })
                .collect(Collectors.toList());

    }

    public Containers getContainerById(Long id) {
        Optional<Containers> optionalContainers = containersRepository.findById(id);
        if (optionalContainers.isPresent()) {
            Containers containers = optionalContainers.get();
            return containers;
        } else {
            throw new RuntimeException("Conteneur non trouvé avec l'ID : " + id);
        }
    }

    public String retrieveContainerToHArbor(Long id, Long userId, Long harborId) {
        Containers containers = containersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Container not found with ID: " + id));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        Harbor harbor = harborRepository.findById(harborId)
                .orElseThrow(() -> new RuntimeException("harbor not found with ID: " + id));

        containers.setStatus(StatusEnum.RETRIEVE);
        containers.setUser(user);
        containers.setHarbor(harbor);
        containersRepository.save(containers);
        return "Container deleted successfully";
    }
}
