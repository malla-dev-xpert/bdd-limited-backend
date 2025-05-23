package com.xpertpro.bbd_project.services;

import ch.qos.logback.classic.Logger;
import com.xpertpro.bbd_project.dto.Package.PackageCreateDto;
import com.xpertpro.bbd_project.dto.achats.CreateAchatDto;
import com.xpertpro.bbd_project.dto.achats.CreateLigneDto;
import com.xpertpro.bbd_project.entity.*;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    PackageRepository packageRepository;

    @Autowired
    ContainersRepository containersRepository;

    @Autowired
    WarehouseRepository warehouseRepository;

    @Transactional
    public String createAchatForClient(Long clientId, Long supplierId, Long userId, Long warehouseId,
                                       Long containerId, CreateAchatDto dto) {
        try {
            // Validation des IDs
            if (clientId == null || supplierId == null || userId == null ||
                    warehouseId == null || containerId == null || dto == null) {
                throw new IllegalArgumentException("Les paramètres ne peuvent pas être null");
            }

            // Récupération des entités
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Utilisateur avec ID " + userId + " introuvable"));
            Partners partner = partnerRepository.findById(clientId)
                    .orElseThrow(() -> new EntityNotFoundException("Client introuvable"));
            Partners supplier = partnerRepository.findById(supplierId)
                    .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable"));
            Warehouse warehouse = warehouseRepository.findById(warehouseId)
                    .orElseThrow(() -> new EntityNotFoundException("Entrepot introuvable"));
            Containers containers = containersRepository.findById(containerId)
                    .orElseThrow(() -> new EntityNotFoundException("Conteneur introuvable"));

            // Validation du versement
            if (dto.getVersementId() == null) {
                return "VERSEMENT_REQUIS";
            }

            Versements versement = versementRepo.findById(dto.getVersementId())
                    .orElseThrow(() -> new EntityNotFoundException("Versement avec ID " + dto.getVersementId() + " introuvable"));

            // Vérification que le versement appartient bien au client
            if (!versement.getPartner().getId().equals(clientId)) {
                return "INVALID_VERSEMENT";
            }

            // Vérification du statut du versement
            if (versement.getStatus() != StatusEnum.CREATE) {
                return "INACTIF_VERSEMENT";
            }

            // Calcul du montant total de l'achat
            double totalAchat = dto.getLignes().stream()
                    .mapToDouble(l -> l.getPrixUnitaire() * l.getQuantityItem())
                    .sum();

            // Vérification du solde disponible (décommenté et amélioré)
//            if (versement.getMontantRestant() < totalAchat) {
//                return "SOLDE_INSUFFISANT";
//            }

            // Création du colis
            PackageCreateDto packDto = dto.getPackageDto();
            Packages pack = new Packages();
            pack.setReference(packDto.getReference());
            pack.setWeight(packDto.getWeight());
            pack.setDimensions(packDto.getDimensions());
            pack.setCreatedAt(packDto.getCreatedAt() != null ? packDto.getCreatedAt() : LocalDateTime.now());
            pack.setPartner(partner);
            pack.setWarehouse(warehouse);
            pack.setContainer(containers);
            pack.setUser(user);
            packageRepository.save(pack);

            // Création de l'achat
            Achats achat = new Achats();
            achat.setCreatedAt(LocalDateTime.now());
            achat.setStatus(StatusEnum.CREATE);
            achat.setFournisseur(supplier);
            achat.setClient(partner);
            achat.setVersement(versement);
            achat.setMontantTotal(totalAchat);
            achatRepository.save(achat);

            // Création des items et lignes d'achat
            List<Items> itemsToSave = new ArrayList<>();
            List<LigneAchat> lignesToAdd = new ArrayList<>();
            LocalDateTime creationDate = packDto.getCreatedAt() != null ? packDto.getCreatedAt() : LocalDateTime.now();

            for (CreateLigneDto ligneDto : dto.getLignes()) {
                Items item = new Items();
                item.setDescription(ligneDto.getDescriptionItem());
                item.setQuantity(ligneDto.getQuantityItem());
                item.setUnitPrice(ligneDto.getPrixUnitaire());
                item.setUser(user);
                item.setAchats(achat);
                item.setPackages(pack);
                item.setStatus(StatusEnum.CREATE);
                item.setCreatedAt(creationDate);
                itemsToSave.add(item);

                LigneAchat ligne = new LigneAchat();
                ligne.setAchats(achat);
                ligne.setItem(item);
                ligne.setQuantite(ligneDto.getQuantityItem());
                ligne.setPrixTotal(ligneDto.getQuantityItem() * ligneDto.getPrixUnitaire());
                ligne.setStatus(StatusEnum.CREATE);
                lignesToAdd.add(ligne);
            }


            // Mettre à jour le solde du partenaire
            Double nouveauSolde = partner.getBalance() - totalAchat;
            partner.setBalance(nouveauSolde);
            partnerRepository.save(partner); // Sauvegarder la mise à jour du solde

            // Sauvegarde en batch
            itemsRepository.saveAll(itemsToSave);
            achat.getLignes().addAll(lignesToAdd);
            achatRepository.save(achat);

            // Mise à jour du montant restant du versement (APRES la création de l'achat)
            versement.setMontantRestant(versement.getMontantRestant() - totalAchat);
            versement.setEditedAt(LocalDateTime.now());
            versementRepo.save(versement);

            return "SUCCESS";
        } catch (EntityNotFoundException e) {
            return "ENTITE_INTROUVABLE";
        } catch (IllegalArgumentException e) {
            return "PARAMETRE_INVALIDE";
        } catch (Exception e) {
            return "ERREUR_INATTENDUE";
        }
    }

}
