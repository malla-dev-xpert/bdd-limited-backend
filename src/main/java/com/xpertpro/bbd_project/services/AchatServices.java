package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.achats.AchatDto;
import com.xpertpro.bbd_project.dto.achats.CreateAchatDto;
import com.xpertpro.bbd_project.dto.achats.CreateItemsDto;
import com.xpertpro.bbd_project.dto.items.ItemDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    DevisesRepository deviseRepository;

    @Transactional
    public String createAchatForClient(Long clientId, Long userId, CreateAchatDto dto) {
        System.out.println("Début création achat - Client: {}, User: {}"+ clientId+ userId);

        try {
            // 1. Validation des entrées
            System.out.println("Validation des entrées...");
            if (clientId == null || userId == null || dto == null) {
                throw new IllegalArgumentException("Paramètres invalides");
            }

            // 2. Récupération user et client
            System.out.println("Récupération user {} et client {}..."+ userId+ clientId);
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User non trouvé"));
            Partners client = partnerRepository.findById(clientId)
                    .orElseThrow(() -> new EntityNotFoundException("Client non trouvé"));

            // 3. Gestion du versement
            Versements versement = null;
            if (dto.getVersementId() != null) {
                System.out.println("Récupération versement {}..."+ dto.getVersementId());
                versement = versementRepo.findById(dto.getVersementId())
                        .orElseThrow(() -> new EntityNotFoundException("Versement non trouvé"));

                System.out.println("Vérification appartenance versement...");
                if (!versement.getPartner().getId().equals(clientId)) {
                    throw new BusinessException("VERSEMENT_CLIENT_MISMATCH",
                            "Le versement n'appartient pas au client");
                }

                System.out.println("Devise versement: {}"+
                        versement.getDevise() != null ? versement.getDevise().getCode() : "null");
            }

            // 4. Traitement des articles
            System.out.println("Traitement de {} articles..."+ dto.getItems().size());
            double total = 0;
            List<Items> items = new ArrayList<>();

            for (CreateItemsDto ligneDto : dto.getItems()) {
                System.out.println("Article: {}"+ ligneDto.getDescription());

                // Validation article
                if (ligneDto.getQuantity() <= 0 || ligneDto.getUnitPrice() <= 0) {
                    throw new BusinessException("INVALID_ITEM",
                            "Quantité ou prix unitaire invalide");
                }

                Partners supplier = partnerRepository.findById(ligneDto.getSupplierId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Fournisseur non trouvé: " + ligneDto.getSupplierId()));

                double itemTotal = ligneDto.getQuantity() * ligneDto.getUnitPrice();
                total += itemTotal;

                Items item = new Items();
                item.setDescription(ligneDto.getDescription());
                item.setQuantity(ligneDto.getQuantity());
                item.setUnitPrice(ligneDto.getUnitPrice());
                item.setUser(user);
                item.setSupplier(supplier);
                item.setInvoiceNumber(ligneDto.getInvoiceNumber());
                item.setStatus(StatusEnum.PENDING);
                item.setCreatedAt(LocalDateTime.now());
                item.setTotalPrice(itemTotal);
                item.setSalesRate(ligneDto.getSalesRate());

                items.add(item);
            }

            // 5. Création de l'achat
            Achats achat = new Achats();
            achat.setClient(client);
            achat.setCreatedAt(LocalDateTime.now());
            achat.setStatus(StatusEnum.PENDING);
            achat.setIsDebt(dto.getVersementId() == null);

            if (versement != null) {
                achat.setVersement(versement);
                achat.setDevise(versement.getDevise());
                achat.setTauxUtilise(1.0); // CNY -> pas de conversion
            } else {
                Devises deviseCNY = deviseRepository.findByCode("CNY")
                        .orElseThrow(() -> new BusinessException("DEVISE_CNY_NOT_FOUND",
                                "Devise CNY non configurée"));
                achat.setDevise(deviseCNY);
                achat.setTauxUtilise(1.0);
            }

            achat.setMontantTotal(total);
            System.out.println("Montant total achat: {}"+ total);

            // 6. Sauvegarde
            System.out.println("Sauvegarde achat...");
            Achats savedAchat = achatRepository.save(achat);
            System.out.println("Achat sauvegardé avec ID: {}"+ savedAchat.getId());

            items.forEach(item -> item.setAchats(savedAchat));
            itemsRepository.saveAll(items);
            System.out.println("{} articles sauvegardés"+ items.size());

            if (achat.getIsDebt()) {
                client.setTotalDebt(client.getTotalDebt() + total);
                partnerRepository.save(client);
            }

            return achat.getIsDebt() ? "ACHAT_CREATED_AS_DEBT_SUCCESSFULLY"
                    : "ACHAT_CREATED_SUCCESSFULLY";

        } catch (Exception e) {
            System.out.println("ERREUR création achat"+ e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur création achat: " + e.getMessage(),
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
                throw new BusinessException("ITEM_ALREADY_RECEIVED", "Item already received");
            }

            Achats achat = item.getAchats();
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

            if (achat.getIsDebt()) {
                // Cas d'un achat en crédit (sans versement initial)
                // On réduit la balance du client (la balance devient négative)
                client.setBalance(client.getBalance() - montantTotal);
            } else {
                // Cas normal avec versement
                // Mise à jour des soldes
                client.setBalance(client.getBalance() - montantTotal);
                versement.setMontantRestant(versement.getMontantRestant() - montantTotal);
                versementRepo.save(versement);
            }

            partnerRepository.save(client);

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

    public List<AchatDto> getAll(int page) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Achats> achats = achatRepository.findByStatusNot(StatusEnum.DELETE, pageable);

        return achats.getContent().stream()
                .filter(achat -> achat.getStatus() != StatusEnum.DELETE)
                .map(achat -> {
                    AchatDto dto = new AchatDto();
                    dto.setId(achat.getId());

                    if (achat.getClient() != null) {
                        dto.setClient(achat.getClient().getFirstName() + " " + achat.getClient().getLastName());
                        dto.setClientPhone(achat.getClient().getPhoneNumber());
                        dto.setClientId(achat.getClient().getId());
                    } else {
                        dto.setClient(null);
                        dto.setClientPhone(null);
                    }

                    dto.setReferenceVersement(achat.getVersement() != null
                            ? achat.getVersement().getReference()
                            : null);
                    dto.setMontantTotal(achat.getMontantTotal());
                    dto.setStatus(achat.getStatus().name());
                    dto.setIsDebt(achat.getIsDebt());
                    dto.setCreatedAt(achat.getCreatedAt() != null ? achat.getCreatedAt() : null);

                    List<ItemDto> itemsDtos = achat.getItems().stream()
                            .map(i -> {
                                ItemDto itemDto = new ItemDto();
                                itemDto.setId(i.getId());
                                itemDto.setDescription(i.getDescription());
                                itemDto.setQuantity(i.getQuantity());
                                itemDto.setUnitPrice(i.getUnitPrice());
                                itemDto.setTotalPrice(i.getTotalPrice());
                                itemDto.setInvoiceNumber(i.getInvoiceNumber());

                                if (i.getSupplier() != null) {
                                    itemDto.setSupplierName(i.getSupplier().getFirstName() + " " + i.getSupplier().getLastName());
                                    itemDto.setSupplierPhone(i.getSupplier().getPhoneNumber());
                                } else {
                                    itemDto.setSupplierName(null);
                                    itemDto.setSupplierPhone(null);
                                }

                                itemDto.setStatus(i.getStatus().name());
                                return itemDto;
                            }).collect(Collectors.toList());

                    dto.setItems(itemsDtos);
                    return dto;
                })
                .collect(Collectors.toList());
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
