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
    ExchangeRateServices exchangeRateServices;

    @Transactional
    public String createAchatForClient(Long clientId, Long userId, CreateAchatDto dto) {
        // Validate input parameters
        if (clientId == null || userId == null || dto == null) {
            throw new IllegalArgumentException("ID client, ID utilisateur et DTO ne peuvent pas être nuls");
        }

        try {
            // Fetch and validate user
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec id: " + userId));

            // Fetch and validate client
            Partners client = partnerRepository.findById(clientId)
                    .orElseThrow(() -> new EntityNotFoundException("Client non trouvé avec id: " + clientId));

            // Validate payment
            if (dto.getVersementId() == null) {
                throw new BusinessException("VERSEMENT_ID_REQUIRED", "ID de paiement est requis");
            }

            Versements versement = versementRepo.findById(dto.getVersementId())
                    .orElseThrow(() -> new EntityNotFoundException("Paiement non trouvé avec id: " + dto.getVersementId()));

            // Verify payment belongs to client
            if (!versement.getPartner().getId().equals(clientId)) {
                throw new BusinessException("VERSEMENT_CLIENT_MISMATCH",
                        "Le paiement n’appartient pas au client spécifié");
            }

            // Validate items
            if (dto.getItems() == null || dto.getItems().isEmpty()) {
                throw new BusinessException("NO_ITEMS_PROVIDED", "Au moins un article est requis");
            }

            // Create purchase
            Achats achat = new Achats();
            achat.setClient(client);
            achat.setVersement(versement);
            achat.setCreatedAt(LocalDateTime.now());
            achat.setStatus(StatusEnum.PENDING);

            // Calculate total amount and validate items
            double total = 0;
            List<Items> items = new ArrayList<>();

            for (CreateItemsDto ligneDto : dto.getItems()) {
                // Validate item
                if (ligneDto.getQuantity() == null || ligneDto.getQuantity() <= 0) {
                    throw new BusinessException("INVALID_QUANTITY",
                            "La quantité doit être supérieure à 0 pour tous les articles");
                }

                if (ligneDto.getUnitPrice() <= 0) {
                    throw new BusinessException("INVALID_UNIT_PRICE",
                            "Le prix unitaire doit être supérieur à 0 pour tous les articles");
                }

                if (ligneDto.getDescription() == null || ligneDto.getDescription().trim().isEmpty()) {
                    throw new BusinessException("MISSING_DESCRIPTION",
                            "La description est requise pour tous les articles");
                }

                if (ligneDto.getSupplierId() == null) {
                    throw new BusinessException("SUPPLIER_ID_REQUIRED",
                            "ID fournisseur est requis pour tous les articles");
                }

                double itemTotal = ligneDto.getQuantity() * ligneDto.getUnitPrice();
                total += itemTotal;

                Partners supplier = partnerRepository.findById(ligneDto.getSupplierId())
                        .orElseThrow(() -> new EntityNotFoundException("Fournisseur non trouvé avec id: " + ligneDto.getSupplierId()));

                // Create item (we'll save them after the purchase is created)
                Items item = new Items();
                item.setDescription(ligneDto.getDescription().trim());
                item.setQuantity(ligneDto.getQuantity());
                item.setUnitPrice(ligneDto.getUnitPrice());
                item.setUser(user);
                item.setSupplier(supplier);
                item.setInvoiceNumber(ligneDto.getInvoiceNumber());
                item.setStatus(StatusEnum.PENDING);
                item.setCreatedAt(LocalDateTime.now());
                item.setTotalPrice(itemTotal);

                items.add(item);
            }

            // Handle currency conversion if needed
            Double montantEnUSD = total;
            if (versement.getDevise() != null && !"CNY".equals(versement.getDevise().getCode())) {
                try {
                    Double taux = exchangeRateServices.getRealTimeRate(versement.getDevise().getCode(), "CNY");
                    if (taux == null || taux <= 0) {
                        throw new BusinessException("INVALID_EXCHANGE_RATE",
                                "Impossible d’obtenir un taux de change valide pour la devise: " + versement.getDevise().getCode());
                    }
                    montantEnUSD = total * taux;
                    achat.setDevise(versement.getDevise());
                    achat.setTauxUtilise(taux);
                } catch (Exception e) {
                    throw new BusinessException("EXCHANGE_RATE_ERROR",
                            "Erreur lors de la conversion de devise: " + e.getMessage());
                }
            }

            achat.setMontantTotal(montantEnUSD);

            // Save purchase first to get ID
            Achats savedAchat = achatRepository.save(achat);

            // Set purchase reference for all items and save them
            items.forEach(item -> item.setAchats(savedAchat));
            itemsRepository.saveAll(items);

            // Log the action
            logServices.logAction(
                    user,
                    "ACHAT_ARTICLE",
                    "Achats",
                    savedAchat.getId()
            );

            return "ACHAT_CREATED_SUCCESSFULLY";

        } catch (BusinessException e) {
            // Known business exceptions are rethrown directly
            throw e;
        } catch (EntityNotFoundException e) {
            // Entity not found exceptions are rethrown directly
            throw e;
        } catch (Exception e) {
            // Unexpected exceptions are wrapped
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Une erreur inattendue s’est produite lors de la création de l’achat",
                    e
            );
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
                throw new BusinessException("ITEM_ALREADY_RECEIVED", "Payment ID is required");
            }

            Achats achat = item.getAchats();
            Versements versement = achat.getVersement();
            Partners client = achat.getClient();

            // Calcul du montant de l'item en CNY
            double montantItem = item.getQuantity() * item.getUnitPrice();
            if (achat.getDevise() != null && !"CNY".equals(achat.getDevise().getCode())) {
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
//        logServices.logAction(user, "CONFIRMER_RECEPTION_COLIS", "ACHAT_ITEMS", itemIds);
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
            throw new BusinessException("BALANCE_UPDATE_FAILED", "Payment ID is required");
        }
    }

    public static class BusinessException extends RuntimeException {
        public BusinessException(String message, String paymentIdIsRequired) {
            super(message);
        }
    }
}
