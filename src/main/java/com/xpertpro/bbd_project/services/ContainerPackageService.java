package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.entity.Containers;
import com.xpertpro.bbd_project.entity.Harbor;
import com.xpertpro.bbd_project.entityMapper.EmbarquementRequest;
import com.xpertpro.bbd_project.entity.Packages;
import com.xpertpro.bbd_project.entityMapper.HarborEmbarquementRequest;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.ContainersRepository;
import com.xpertpro.bbd_project.repository.HarborRepository;
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
    private final HarborRepository harborRepository;

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

    @Transactional
    public String embarquerConteneursDansPort(HarborEmbarquementRequest request) {
        Harbor harbor = harborRepository.findById(request.getHarborId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Port non trouvé avec l'ID: " + request.getHarborId()));

        if (harbor.getStatus() != StatusEnum.CREATE) {
            return "HARBOR_NOT_AVAILABLE";
        }

        List<Containers> containers = containerRepository.findAllById(request.getContainerId());

        // Vérifier si les conteneurs sont déjà dans un autre port
        containers.forEach(container -> {
            if (container.getHarbor() != null && !container.getHarbor().getId().equals(harbor.getId())) {
                throw new OperationNotAllowedException(
                        "Le conteneur " + container.getReference() + " est déjà dans le port " + container.getHarbor().getId());
            }
        });

//        containers.forEach(container -> {
//            if (container.getStatus() != StatusEnum.PENDING && container.getStatus() != StatusEnum.IN_CONTAINER) {
//                throw new OperationNotAllowedException(
//                        "Le conteneur " + container.getReference() + " n'a pas le statut approprié (PENDING ou IN_CONTAINER)");
//            }
//        });

        containers.forEach(container -> {
            container.setHarbor(harbor);
//            container.setStatus(StatusEnum.IN_HARBOR);
            container.setEditedAt(LocalDateTime.now());
        });

        harbor.getContainers().addAll(containers);
//        harbor.setStatus(StatusEnum.PENDING);
        harbor.setEditedAt(LocalDateTime.now());

        containerRepository.saveAll(containers);
        harborRepository.save(harbor);
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