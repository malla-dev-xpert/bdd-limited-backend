package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.entity.Containers;
import com.xpertpro.bbd_project.entity.EmbarquementRequest;
import com.xpertpro.bbd_project.entity.Packages;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.ContainersRepository;
import com.xpertpro.bbd_project.repository.PackageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContainerPackageService {

    private final ContainersRepository containerRepository;
    private final PackageRepository packageRepository;

    @Transactional
    public String embarquerColis(EmbarquementRequest request) {
        Containers container = containerRepository.findById(request.getContainerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conteneur non trouvé avec l'ID: " + request.getContainerId()));

        if (!Boolean.TRUE.equals(container.getIsAvailable())) {
            return "CONTAINER_NOT_AVAILABLE";
        }

        if (container.getStatus() != StatusEnum.PENDING) {
            return "CONTAINER_NOT_IN_PENDING";
        }

        List<Packages> packages = packageRepository.findAllById(request.getPackageId());

        // Vérifier si les colis sont déjà dans un autre conteneur
        packages.forEach(pkg -> {
            if (pkg.getContainer() != null && !pkg.getContainer().getId().equals(container.getId())) {
                throw new OperationNotAllowedException(
                        "Le colis " + pkg.getReference() + " est déjà dans le conteneur " + pkg.getContainer().getId());
            }
        });

        packages.forEach(pkg -> {
            if (pkg.getStatus() != StatusEnum.RECEIVED) {
                throw new OperationNotAllowedException(
                        "Le colis " + pkg.getReference() + " n'est pas en statut RECEIVED");
            }
        });

        packages.forEach(pkg -> {
            pkg.setContainer(container);
            pkg.setStatus(StatusEnum.IN_CONTAINER);
            pkg.setEditedAt(LocalDateTime.now());
        });

        container.getPackages().addAll(packages);
        container.setEditedAt(LocalDateTime.now());

        packageRepository.saveAll(packages);
        containerRepository.save(container);
        return "SAVED";
    }


    public class OperationNotAllowedException extends RuntimeException {
        public OperationNotAllowedException(String message) {
            super(message);
        }
    }


    public class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}