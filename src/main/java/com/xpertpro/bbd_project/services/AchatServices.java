package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.Package.PackageCreateDto;
import com.xpertpro.bbd_project.dto.achats.CreateAchatDto;
import com.xpertpro.bbd_project.dto.achats.CreateLigneDto;
import com.xpertpro.bbd_project.entity.*;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.*;
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

    @Transactional
    public void createAchatForClient(Long clientId, Long supplierId, Long userId, CreateAchatDto dto) {
        // Récupération des entités
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur avec ID " + userId + " introuvable"));
        Partners partner = partnerRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        Partners supplier = partnerRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Fournisseur introuvable"));

        // Récupération du dernier versement actif
        Versements versement = versementRepo.findFirstByPartnerIdAndStatusOrderByCreatedAtDesc(
                clientId, StatusEnum.CREATE
        ).orElseThrow(() -> new RuntimeException("Aucun versement actif"));

        // Calcul du montant total de l'achat
        double totalAchat = dto.getLignes().stream()
                .mapToDouble(l -> l.getPrixUnitaire() * l.getQuantityItem())
                .sum();

        // Vérification du solde disponible
//        if (versement.getMontantRestant() < totalAchat) {
//            throw new RuntimeException("Solde insuffisant pour effectuer cet achat");
//        }

        // Création du colis
        PackageCreateDto packDto = dto.getPackageDto();
        Packages pack = new Packages();
        pack.setReference(packDto.getReference());
        pack.setWeight(packDto.getWeight());
        pack.setDimensions(packDto.getDimensions());
        pack.setCreatedAt(packDto.getCreatedAt() != null ? packDto.getCreatedAt() : LocalDateTime.now());
        packageRepository.save(pack);

        // Création de l'achat
        Achats achat = new Achats();
        achat.setCreatedAt(LocalDateTime.now());
        achat.setStatus(StatusEnum.CREATE);
        achat.setFournisseur(supplier);
        achat.setClient(partner);
        achat.setVersement(versement);
        achatRepository.save(achat);

        // Préparation des items et lignes pour sauvegarde en batch
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

        // Mise à jour du montant restant
        versement.setMontantRestant(versement.getMontantRestant() - totalAchat);
        versement.setEditedAt(LocalDateTime.now());
        versementRepo.save(versement);

        // Sauvegarde en batch pour meilleure performance
        itemsRepository.saveAll(itemsToSave);
        achat.getLignes().addAll(lignesToAdd);
        achatRepository.save(achat);
    }

//    private AchatDto toAchatDto(Achats achat) {
//        AchatDto dto = new AchatDto();
//        dto.setId(achat.getId());
//        dto.setCreatedAt(achat.getCreatedAt());
//        dto.setReferenceVersement(achat.getVersement().getReference());
//        dto.setMontantRestant(achat.getVersement().getMontantRestant());
//        dto.setMontantVerser(achat.getVersement().getMontantVerser());
//
//        if (achat.getFournisseur() != null) {
//            dto.setFournisseur(achat.getFournisseur().getFirstName() + " " + achat.getFournisseur().getLastName());
//            dto.setFournisseurPhone(achat.getFournisseur().getPhoneNumber());
//        }
//
//        List<LigneAchatDto> ligneDtos = achat.getLignes().stream().map(ligne -> {
//            LigneAchatDto ligneDto = new LigneAchatDto();
//            ligneDto.setId(ligne.getId());
//            ligneDto.setAchatId(achat.getId());
//            ligneDto.setItemId(ligne.getItem().getId());
//            ligneDto.setDescriptionItem(ligne.getItem().getDescription());
//            ligneDto.setQuantityItem(ligne.getItem().getQuantity());
//            ligneDto.setUnitPriceItem(ligne.getPrixTotal() / ligne.getQuantite());
//            ligneDto.setQuantity(ligne.getQuantite());
//            ligneDto.setPrixTotal(ligne.getPrixTotal());
//            return ligneDto;
//        }).collect(Collectors.toList());
//
//        dto.setLignes(ligneDtos);
//        return dto;
//    }
}
