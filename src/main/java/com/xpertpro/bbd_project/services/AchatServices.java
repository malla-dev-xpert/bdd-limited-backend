package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.achats.CreateAchatDto;
import com.xpertpro.bbd_project.dto.achats.CreateLigneDto;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    LigneAchatRepository ligneAchatRepository;
    @Autowired
    LogServices logServices;

    @Transactional
    public String createAchatForClient(Long clientId, Long supplierId, Long userId, CreateAchatDto dto) {
        try {

            // 1. Validation des entités
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            Partners client = partnerRepository.findById(clientId)
                    .orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + clientId));

            Partners supplier = partnerRepository.findById(supplierId)
                    .orElseThrow(() -> new EntityNotFoundException("Supplier not found with id: " + supplierId));

            // 2. Validation du versement
            if (dto.getVersementId() == null) {
                throw new BusinessException("VERSEMENT_ID_REQUIRED");
            }

            Versements versement = versementRepo.findById(dto.getVersementId())
                    .orElseThrow(() -> new EntityNotFoundException("Versement not found with id: " + dto.getVersementId()));

            if (!versement.getPartner().getId().equals(clientId)) {
                throw new BusinessException("VERSEMENT_CLIENT_MISMATCH");
            }

            // 3. Création de l'achat
            Achats achat = new Achats();
            achat.setClient(client);
            achat.setFournisseur(supplier);
            achat.setVersement(versement);
            achat.setCreatedAt(LocalDateTime.now());
            achat.setStatus(StatusEnum.CREATE);

            // 4. Calcul du montant total
            double total = dto.getLignes().stream()
                    .mapToDouble(l -> {
                        if (l.getQuantityItem() <= 0 || l.getPrixUnitaire() <= 0) {
                            throw new BusinessException("INVALID_ITEM_VALUES");
                        }
                        return l.getQuantityItem() * l.getPrixUnitaire();
                    })
                    .sum();
            achat.setMontantTotal(total);

            // 5. Sauvegarde de l'achat (pour obtenir l'ID)
            Achats savedAchat = achatRepository.save(achat);

            // 6. Création des items et lignes
            List<Items> items = new ArrayList<>();
            List<LigneAchat> lignes = new ArrayList<>();

            for (CreateLigneDto ligneDto : dto.getLignes()) {
                Items item = new Items();
                item.setDescription(ligneDto.getDescriptionItem());
                item.setQuantity(ligneDto.getQuantityItem());
                item.setUnitPrice(ligneDto.getPrixUnitaire());
                item.setUser(user);
                item.setAchats(savedAchat);
                item.setStatus(StatusEnum.CREATE);
                item.setCreatedAt(LocalDateTime.now());
                items.add(item);

                LigneAchat ligne = new LigneAchat();
                ligne.setAchats(savedAchat);
                ligne.setItem(item);
                ligne.setQuantite(ligneDto.getQuantityItem());
                ligne.setPrixTotal(ligneDto.getQuantityItem() * ligneDto.getPrixUnitaire());
                ligne.setStatus(StatusEnum.CREATE);
                lignes.add(ligne);
            }

            // 7. Sauvegarde en cascade
            itemsRepository.saveAll(items);
            ligneAchatRepository.saveAll(lignes);

            // 8. Mise à jour des soldes
            updateBalances(client, versement, total);

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
//            log.error("Erreur lors de la mise à jour des soldes", e);
            throw new BusinessException("BALANCE_UPDATE_FAILED");
        }
    }

    public class BusinessException extends RuntimeException {
        public BusinessException(String message) {
            super(message);
        }
    }

}
