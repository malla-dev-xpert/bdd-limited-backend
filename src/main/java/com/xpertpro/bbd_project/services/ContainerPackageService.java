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
    public Containers embarquerColis(EmbarquementRequest request) {
        Containers container = containerRepository.findById(request.getContainerId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Conteneur non trouvé avec l'ID: " + request.getContainerId()));

        if (!Boolean.TRUE.equals(container.getIsAvailable())) {
            throw new OperationNotAllowedException("Le conteneur n'est pas disponible pour l'embarquement");
        }

        if (container.getStatus() != StatusEnum.PENDING) {
            throw new OperationNotAllowedException(
                    "Seuls les conteneurs en statut PENDING peuvent recevoir des colis");
        }

        List<Packages> packages = packageRepository.findAllById(request.getPackageId());

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
        return containerRepository.save(container);
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