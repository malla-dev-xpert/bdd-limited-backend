package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.ExpeditionDto;
import com.xpertpro.bbd_project.dto.harbor.HarborDto;
import com.xpertpro.bbd_project.dtoMapper.ExpeditionDtoMapper;
import com.xpertpro.bbd_project.entity.*;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.ExpeditionRepository;
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
public class ExpeditionServices {
    @Autowired
    ExpeditionRepository expeditionRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PartnerRepository clientRepo;
    @Autowired
    ExpeditionDtoMapper expeditionDtoMapper;

    public String create(ExpeditionDto dto, Long clientId, Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Partners client = clientRepo.findById(clientId).orElseThrow(() -> new RuntimeException("Client not found"));

        Expeditions expeditions = expeditionDtoMapper.toEntity(dto);

        expeditions.setCreatedAt(LocalDateTime.now());
        expeditions.setClient(client);
        expeditions.setCreatedBy(user);
        expeditionRepository.save(expeditions);
        return "SUCCESS";
    }

    public List<ExpeditionDto> getAll(int page, String query) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Expeditions> expeditions = expeditionRepository.findByStatusNot(StatusEnum.DELETE, pageable);

        if (query != null && !query.isEmpty()) {
            expeditions = expeditionRepository.findByStatusAndSearchQuery(
                    StatusEnum.CREATE,
                    "%" + query.toLowerCase() + "%",
                    pageable
            );
        } else {
            expeditions = expeditionRepository.findByStatusNot(StatusEnum.DELETE, pageable);
        }


        return expeditions.stream()
                .filter(exp -> exp.getStatus() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(Expeditions::getCreatedAt).reversed())
                .map(exp -> {
                    ExpeditionDto dto = new ExpeditionDto();
                    dto.setId(exp.getId());
                    dto.setCbn(exp.getCbn());
                    dto.setRef(exp.getRef());
                    dto.setExpeditionType(exp.getExpeditionType());
                    dto.setItemQuantity(exp.getItemQuantity());
                    dto.setWeight(exp.getWeight());
                    dto.setArrivalDate(exp.getArrivalDate());
                    dto.setStartDate(exp.getStartDate());
                    dto.setStartCountry(exp.getStartCountry());
                    dto.setDestinationCountry(exp.getDestinationCountry());
                    dto.setStatus(exp.getStatus().name());
                    dto.setClientId(exp.getClient() != null ? exp.getClient().getId() : null);
                    dto.setClientPhone(exp.getClient() != null ? exp.getClient().getPhoneNumber() : null);
                    dto.setClientName(exp.getClient() != null ? exp.getClient().getFirstName() + " " + exp.getClient().getLastName() : null);

                    return dto;
                })
                .collect(Collectors.toList());

    }

    @Transactional
    public void startExpedition(Long expeditionId) {
        Expeditions expedition = expeditionRepository.findById(expeditionId)
                .orElseThrow(() -> new EntityNotFoundException("Expédition introuvable avec l'ID : " + expeditionId));

        if (expedition.getStatus() == StatusEnum.INPROGRESS) {
            throw new IllegalStateException("L'expédition est déjà en cours.");
        }

        if (expedition.getStatus() != StatusEnum.PENDING) {
            throw new IllegalStateException("Impossible de démarrer une expédition qui n'est pas en attente.");
        }

        expedition.setStatus(StatusEnum.INPROGRESS);
        expedition.setEditedAt(LocalDateTime.now());
        expeditionRepository.save(expedition);
    }

    @Transactional
    public void confirmExpedition(Long expeditionId) {
        Expeditions expedition = expeditionRepository.findById(expeditionId)
                .orElseThrow(() -> new EntityNotFoundException("Expédition introuvable avec l'ID : " + expeditionId));

        if (expedition.getStatus() == StatusEnum.DELIVERED) {
            throw new IllegalStateException("L'expédition est déjà en arrivé.");
        }

        if (expedition.getStatus() != StatusEnum.INPROGRESS) {
            throw new IllegalStateException("Impossible de confirmer la réception de l'expédition. Elle n'est pas en transit.");
        }

        expedition.setStatus(StatusEnum.DELIVERED);
        expedition.setEditedAt(LocalDateTime.now());
        expeditionRepository.save(expedition);
    }

    @Transactional
    public void receivedExpedition(Long expeditionId) {
        Expeditions expedition = expeditionRepository.findById(expeditionId)
                .orElseThrow(() -> new EntityNotFoundException("Expédition introuvable avec l'ID : " + expeditionId));

        if (expedition.getStatus() == StatusEnum.RECEIVED) {
            throw new IllegalStateException("L'expédition est déjà livrée.");
        }

        if (expedition.getStatus() != StatusEnum.DELIVERED) {
            throw new IllegalStateException("Impossible de confirmer la livraison de l'expédition. Elle n'est pas arrivée a destination.");
        }

        expedition.setStatus(StatusEnum.RECEIVED);
        expedition.setEditedAt(LocalDateTime.now());
        expeditionRepository.save(expedition);
    }

    @Transactional
    public void deleteExpedition(Long expeditionId) {
        Expeditions expedition = expeditionRepository.findById(expeditionId)
                .orElseThrow(() -> new EntityNotFoundException("Expédition introuvable avec l'ID : " + expeditionId));

        if (expedition.getStatus() == StatusEnum.DELETE) {
            throw new IllegalStateException("L'expédition est déjà supprimer.");
        }

        expedition.setStatus(StatusEnum.DELETE);
        expedition.setEditedAt(LocalDateTime.now());
        expeditionRepository.save(expedition);
    }

    public String updateExpedition(Long id, ExpeditionDto newExpedition, Long userId) {
        Optional<Expeditions> optionalExpedition = expeditionRepository.findById(id);
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (optionalExpedition.isEmpty()) {
            throw new RuntimeException("Expedition not found with ID: " + id);
        }

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        Expeditions expedition = optionalExpedition.get();
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

        expeditionRepository.save(expedition);
        return "SUCCESS";
    }

}
