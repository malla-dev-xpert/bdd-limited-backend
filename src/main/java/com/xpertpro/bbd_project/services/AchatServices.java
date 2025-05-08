package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.achats.AchatDto;
import com.xpertpro.bbd_project.dto.achats.CreateAchatDto;
import com.xpertpro.bbd_project.dto.achats.CreateLigneDto;
import com.xpertpro.bbd_project.dto.achats.LigneAchatDto;
import com.xpertpro.bbd_project.entity.*;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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


    @Transactional
    public void createAchatForClient(Long partnerId, Long userId, CreateAchatDto dto) {
        Partners partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Versements versement = versementRepo.findFirstByPartnerIdAndStatusOrderByCreatedAtDesc(
                partnerId, StatusEnum.CREATE
        ).orElseThrow(() -> new RuntimeException("Aucun versement actif"));

        Achats achat = new Achats();
        achat.setCreatedAt(LocalDateTime.now());
        achat.setStatus(StatusEnum.CREATE);
        achat.setFournisseur(partner);
        achat.setVersement(versement);
        achat = achatRepository.save(achat);

        double total = 0;
        for (CreateLigneDto ligneDto : dto.getLignes()) {
            Items item = new Items();
            item.setDescription(ligneDto.getDescriptionItem());
            item.setQuantity(ligneDto.getQuantityItem());
            item.setUser(user);
            item.setAchats(achat);
            item.setUnitPrice(ligneDto.getPrixUnitaire());
            item.setStatus(StatusEnum.CREATE);
            itemsRepository.save(item);

            LigneAchat ligne = new LigneAchat();
            ligne.setAchats(achat);
            ligne.setItem(item);
            ligne.setQuantite(ligneDto.getQuantityItem());
            ligne.setPrixTotal(ligneDto.getQuantityItem() * ligneDto.getPrixUnitaire());
            ligne.setStatus(StatusEnum.CREATE);

            achat.getLignes().add(ligne);
            total += ligne.getPrixTotal();
        }

        // Validation du montant
//        Double montantGlobal = versementRepo.getTotalMontantRestantByPartnerId(partnerId);
//        if (montantGlobal < total) {
//            throw new RuntimeException("Solde insuffisant");
//        }
        versement.setMontantRestant(versement.getMontantRestant() - total);
        versement.setEditedAt(LocalDateTime.now());

        achatRepository.save(achat); // Mise à jour avec le versement
    }


    private AchatDto toAchatDto(Achats achat) {
        AchatDto dto = new AchatDto();
        dto.setId(achat.getId());
        dto.setCreatedAt(achat.getCreatedAt());
        dto.setReferenceVersement(achat.getVersement().getReference());
        dto.setMontantRestant(achat.getVersement().getMontantRestant());
        dto.setMontantVerser(achat.getVersement().getMontantVerser());

        if (achat.getFournisseur() != null) {
            dto.setFournisseur(achat.getFournisseur().getFirstName() + " " + achat.getFournisseur().getLastName());
            dto.setFournisseurPhone(achat.getFournisseur().getPhoneNumber());
        }

        List<LigneAchatDto> ligneDtos = achat.getLignes().stream().map(ligne -> {
            LigneAchatDto ligneDto = new LigneAchatDto();
            ligneDto.setId(ligne.getId());
            ligneDto.setAchatId(achat.getId());
            ligneDto.setItemId(ligne.getItem().getId());
            ligneDto.setDescriptionItem(ligne.getItem().getDescription());
            ligneDto.setQuantityItem(ligne.getItem().getQuantity());
            ligneDto.setUnitPriceItem(ligne.getPrixTotal() / ligne.getQuantite()); // pour déduire le prix unitaire
            ligneDto.setQuantity(ligne.getQuantite());
            ligneDto.setPrixTotal(ligne.getPrixTotal());
            return ligneDto;
        }).collect(Collectors.toList());

        dto.setLignes(ligneDtos);
        return dto;
    }

}
