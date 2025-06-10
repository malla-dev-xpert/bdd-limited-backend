package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.achats.CreateAchatDto;
import com.xpertpro.bbd_project.dto.achats.CreateItemsDto;
import com.xpertpro.bbd_project.entity.*;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AchatServices {

    @Autowired
    VersementRepo versementRepo;
    @Autowired
    PartnerRepository partnerRepository;
    @Autowired
    ItemsRepository itemsRepository;
    @Autowired
    AchatRepository achatRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    LogServices logServices;
    @Autowired
    VersementServices versementServices;

    @Transactional
    public String createAchatForClient(Long clientId, Long supplierId, Long userId, CreateAchatDto dto) {
        try {
            //  Validation des entités
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            Partners client = partnerRepository.findById(clientId)
                    .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + clientId));

            Partners supplier = partnerRepository.findById(supplierId)
                    .orElseThrow(() -> new EntityNotFoundException("Supplier not found with id: " + supplierId));

            // Validation du versement
            if (dto.getVersementId() == null) {
                throw new BusinessException("VERSEMENT_ID_REQUIRED");
            }

            Versements versement = versementRepo.findById(dto.getVersementId())
                    .orElseThrow(() -> new EntityNotFoundException("Versement not found with id: " + dto.getVersementId()));

            if (!versement.getPartner().getId().equals(clientId)) {
                throw new BusinessException("VERSEMENT_CLIENT_MISMATCH");
            }

            // Création de l'achat
            Achats achat = new Achats();
            achat.setClient(client);
            achat.setFournisseur(supplier);
            achat.setVersement(versement);
            achat.setCreatedAt(LocalDateTime.now());
            achat.setInvoiceNumber(dto.getInvoiceNumber());
            achat.setStatus(StatusEnum.PENDING);

            // Calcul du montant total
            double total = dto.getItems().stream()
                    .mapToDouble(l -> {
                        if (l.getQuantity() <= 0 || l.getUnitPrice() <= 0) {
                            throw new BusinessException("INVALID_ITEM_VALUES");
                        }
                        return l.getQuantity() * l.getUnitPrice();
                    })
                    .sum();

            // Conversion en USD
            Double montantEnUSD = total;
            if (versement.getDevise() != null && !"USD".equals(versement.getDevise().getCode())) {
                Double taux = versementServices.getRealTimeRate(versement.getDevise().getCode(), "USD");
                montantEnUSD = total * taux;
                achat.setDevise(versement.getDevise());
                achat.setTauxUtilise(taux);
            }

            achat.setMontantTotal(montantEnUSD);

            // Sauvegarde de l'achat (pour obtenir l'ID)
            Achats savedAchat = achatRepository.save(achat);

            // Création des items et lignes
            List<Items> items = new ArrayList<>();

            for (CreateItemsDto ligneDto : dto.getItems()) {
                Items item = new Items();
                item.setDescription(ligneDto.getDescription());
                item.setQuantity(ligneDto.getQuantity());
                item.setUnitPrice(ligneDto.getUnitPrice());
                item.setUser(user);
                item.setAchats(savedAchat);
                item.setStatus(StatusEnum.PENDING);
                item.setCreatedAt(LocalDateTime.now());
                item.setTotalPrice(ligneDto.getQuantity() * ligneDto.getUnitPrice());
                items.add(item);
            }

            // Sauvegarde en cascade
            itemsRepository.saveAll(items);

            logServices.logAction(
                    user,
                    "ACHAT_ARTICLE",
                    "Achats",
                    achat.getId()
            );

            return "ACHAT_CREATED";

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la création de l'achat", e);
        }
    }

    @Transactional
    public void confirmItemDelivery(List<Long> itemIds, Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Map<Achats, Double> montantsParAchat = new HashMap<>(); // Pour regrouper par achat

        for (Long itemId : itemIds) {
            Items item = itemsRepository.findById(itemId)
                    .orElseThrow(() -> new EntityNotFoundException("Item not found"));

            if (item.getStatus() == StatusEnum.RECEIVED) {
                throw new BusinessException("ITEM_ALREADY_RECEIVED");
            }

            Achats achat = item.getAchats();
            Versements versement = achat.getVersement();
            Partners client = achat.getClient();

            // Calcul du montant de l'item en USD
            double montantItem = item.getQuantity() * item.getUnitPrice();
            if (achat.getDevise() != null && !"USD".equals(achat.getDevise().getCode())) {
                montantItem *= achat.getTauxUtilise();
            }

            // Mise à jour du statut
            item.setStatus(StatusEnum.RECEIVED);
            itemsRepository.save(item);

            // Accumuler les montants par achat
            montantsParAchat.merge(achat, montantItem, Double::sum);
        }

        // Mettre à jour les soldes par achat
        for (Map.Entry<Achats, Double> entry : montantsParAchat.entrySet()) {
            Achats achat = entry.getKey();
            Versements versement = achat.getVersement();
            Partners client = achat.getClient();
            double montantTotal = entry.getValue();

            // Mise à jour des soldes
            client.setBalance(client.getBalance() - montantTotal);
            versement.setMontantRestant(versement.getMontantRestant() - montantTotal);

            partnerRepository.save(client);
            versementRepo.save(versement);

            // Vérifier si tous les items sont livrés
            boolean allDelivered = achat.getItems().stream()
                    .allMatch(i -> i.getStatus() == StatusEnum.RECEIVED);

            if (allDelivered) {
                achat.setStatus(StatusEnum.COMPLETED);
                achatRepository.save(achat);
            }
        }

        // Logging
//        logServices.logAction(user, "ITEMS_CONFIRMED", "Achat", itemIds);
    }

    private void updateBalances(Partners client, Versements versement, double montant) {
        try {
            // Mise à jour client
            client.setBalance(client.getBalance() - montant);
            partnerRepository.save(client);

            // Mise à jour versement
            versement.setMontantRestant(versement.getMontantRestant() - montant);
            versement.setEditedAt(LocalDateTime.now());
            versementRepo.save(versement);
        } catch (Exception e) {
            throw new BusinessException("BALANCE_UPDATE_FAILED");
        }
    }

    public class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }
}
