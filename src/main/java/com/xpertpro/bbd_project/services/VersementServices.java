package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.achats.AchatDto;
import com.xpertpro.bbd_project.dto.achats.LigneAchatDto;
import com.xpertpro.bbd_project.dto.achats.VersementDto;
import com.xpertpro.bbd_project.entity.*;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.AchatRepository;
import com.xpertpro.bbd_project.repository.PartnerRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import com.xpertpro.bbd_project.repository.VersementRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VersementServices {
    @Autowired
    private VersementRepo versementRepo;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PartnerRepository partnerRepository;
    @Autowired
    private AchatRepository achatRepository;

    @Transactional
    public String newVersement(Long userId, Long partnerId, VersementDto dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Partners partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partenaire introuvable"));

        // Créer un nouveau versement
        Versements newVersement = new Versements();
        newVersement.setMontantVerser(dto.getMontantVerser());
        newVersement.setMontantRestant(dto.getMontantVerser());
        newVersement.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
        newVersement.setUser(user);
        newVersement.setPartner(partner);
        newVersement.setStatus(StatusEnum.CREATE);

        // Mettre à jour le solde du partenaire
        Double nouveauSolde = partner.getBalance() + dto.getMontantVerser();
        partner.setBalance(nouveauSolde);
        partnerRepository.save(partner); // Sauvegarder la mise à jour du solde

        versementRepo.save(newVersement);

        // Générer une référence
        String ref = String.format("BBDPAY-%02d", newVersement.getId());
        newVersement.setReference(ref);

        versementRepo.save(newVersement);

        return ref;
    }

    public List<VersementDto> getAll(int page) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Versements> versements = versementRepo.findByStatusNot(StatusEnum.DELETE, pageable);

        return versements.stream()
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(Versements::getCreatedAt).reversed())
                .map(pkg -> {
                    VersementDto dto = new VersementDto();
                    dto.setId(pkg.getId());
                    dto.setReference(pkg.getReference());
                    dto.setMontantVerser(pkg.getMontantVerser());
                    dto.setMontantRestant(pkg.getMontantRestant()); // Récupération directe depuis le versement
                    dto.setCreatedAt(pkg.getCreatedAt());
                    dto.setEditedAt(pkg.getEditedAt());
                    dto.setCliendId(pkg.getPartner() != null
                            ? pkg.getPartner().getId()
                            : null);
                    dto.setPartnerName(pkg.getPartner() != null
                            ? pkg.getPartner().getFirstName() + " " + pkg.getPartner().getLastName()
                            : null);
                    dto.setPartnerAccountType(pkg.getPartner() != null
                            ? pkg.getPartner().getAccountType()
                            : null);
                    dto.setPartnerPhone(pkg.getPartner() != null
                            ? pkg.getPartner().getPhoneNumber()
                            : null);
                    dto.setPartnerCountry(pkg.getPartner() != null
                            ? pkg.getPartner().getCountry()
                            : null);

                    List<AchatDto> achatDtos = pkg.getAchats().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .map(item -> {
                                AchatDto achatDto = new AchatDto();
                                achatDto.setId(item.getId());
                                achatDto.setFournisseur(item.getFournisseur() != null
                                        ? item.getFournisseur().getFirstName() + " " + item.getFournisseur().getLastName()
                                        : null);
                                achatDto.setFournisseurPhone(item.getFournisseur() != null
                                        ? item.getFournisseur().getPhoneNumber()
                                        : null);
                                // Utilisation des montants du versement parent
                                achatDto.setMontantRestant(pkg.getMontantRestant());
                                achatDto.setMontantVerser(pkg.getMontantVerser());
                                achatDto.setReferenceVersement(pkg.getReference());

                                List<LigneAchatDto> ligneDtos = item.getLignes().stream()
                                        .map(ligne -> {
                                            LigneAchatDto ligneDto = new LigneAchatDto();
                                            ligneDto.setId(ligne.getId());
                                            ligneDto.setAchatId(ligne.getAchats() != null
                                                    ? ligne.getAchats().getId()
                                                    : null);
                                            ligneDto.setQuantity(ligne.getQuantite());
                                            ligneDto.setPrixTotal(ligne.getPrixTotal());
                                            ligneDto.setItemId(ligne.getItem() != null
                                                    ? ligne.getItem().getId()
                                                    : null);
                                            ligneDto.setDescriptionItem(ligne.getItem() != null
                                                    ? ligne.getItem().getDescription()
                                                    : null);
                                            ligneDto.setQuantityItem(ligne.getItem().getQuantity());
                                            ligneDto.setUnitPriceItem(ligne.getItem().getUnitPrice());
                                            return ligneDto;
                                        }).collect(Collectors.toList());

                                achatDto.setLignes(ligneDtos);

                                return achatDto;
                            }).collect(Collectors.toList());

                    dto.setAchats(achatDtos);

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<VersementDto> getByClientId(Long clientId, int page) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        // Récupération des versements pour le client spécifié
        Page<Versements> versements = versementRepo.findByPartnerIdAndStatusNot(clientId, StatusEnum.DELETE, pageable);

        return versements.stream()
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(Versements::getCreatedAt).reversed())
                .map(pkg -> {
                    VersementDto dto = new VersementDto();
                    dto.setId(pkg.getId());
                    dto.setReference(pkg.getReference());
                    dto.setMontantRestant(pkg.getMontantRestant());
                    dto.setMontantVerser(pkg.getMontantVerser());
                    dto.setCreatedAt(pkg.getCreatedAt());
                    dto.setEditedAt(pkg.getEditedAt());

                    // Info partenaire (client)
                    if (pkg.getPartner() != null) {
                        dto.setPartnerName(pkg.getPartner().getFirstName() + " " + pkg.getPartner().getLastName());
                        dto.setPartnerPhone(pkg.getPartner().getPhoneNumber());
                        dto.setPartnerCountry(pkg.getPartner().getCountry());
                    }

                    // Liste des achats associés
                    List<AchatDto> achatDtos = pkg.getAchats().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .map(item -> {
                                AchatDto achatDto = new AchatDto();
                                achatDto.setId(item.getId());

                                // Info fournisseur
                                if (item.getFournisseur() != null) {
                                    achatDto.setFournisseur(
                                            item.getFournisseur().getFirstName() + " " + item.getFournisseur().getLastName()
                                    );
                                    achatDto.setFournisseurPhone(item.getFournisseur().getPhoneNumber());
                                }

                                // Info versement
                                if (item.getVersement() != null) {
                                    achatDto.setMontantRestant(item.getVersement().getMontantRestant());
                                    achatDto.setMontantVerser(item.getVersement().getMontantVerser());
                                    achatDto.setReferenceVersement(item.getVersement().getReference());
                                }

                                // Lignes d'achat
                                List<LigneAchatDto> ligneDtos = item.getLignes().stream()
                                        .map(ligne -> {
                                            LigneAchatDto ligneDto = new LigneAchatDto();
                                            ligneDto.setId(ligne.getId());
                                            ligneDto.setAchatId(ligne.getAchats() != null ? ligne.getAchats().getId() : null);
                                            ligneDto.setQuantity(ligne.getQuantite());
                                            ligneDto.setPrixTotal(ligne.getPrixTotal());

                                            if (ligne.getItem() != null) {
                                                ligneDto.setItemId(ligne.getItem().getId());
                                                ligneDto.setDescriptionItem(ligne.getItem().getDescription());
                                                ligneDto.setQuantityItem(ligne.getItem().getQuantity());
                                                ligneDto.setUnitPriceItem(ligne.getItem().getUnitPrice());
                                            }

                                            return ligneDto;
                                        }).collect(Collectors.toList());

                                achatDto.setLignes(ligneDtos);
                                return achatDto;
                            }).collect(Collectors.toList());

                    dto.setAchats(achatDtos);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public boolean updateVersement(Long id, Long userId, Long clientId, VersementDto dto) {
        Versements versement = versementRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Versement non trouvé"));

        Partners client = partnerRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client non trouvé"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        Double ancienMontantVerser = versement.getMontantVerser();
        Double nouveauMontantVerser = dto.getMontantVerser();

        if(nouveauMontantVerser != null) {
            versement.setMontantVerser(nouveauMontantVerser);
        }

        versement.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
        versement.setPartner(client);
        versement.setUser(user);

        if (nouveauMontantVerser != null) {
            if (versement.getMontantRestant() == null) {
                versement.setMontantRestant(nouveauMontantVerser);
            } else {
                if (ancienMontantVerser.equals(versement.getMontantRestant())) {
                    versement.setMontantRestant(nouveauMontantVerser);
                } else {
                    double difference = nouveauMontantVerser - ancienMontantVerser;
                    versement.setMontantRestant(versement.getMontantRestant() + difference);
                }
            }
        }

        versementRepo.save(versement);

        return true;
    }

    public String deleteVersement(Long id, Long userId) {
        try {
            Versements versement = versementRepo.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Versement non trouvé"));

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

            boolean hasAchat = achatRepository.existsByVersementId(id);
            if (hasAchat) {
                return "IMPOSSIBLE";
            }

            versement.setStatus(StatusEnum.DELETE);
            versement.setEditedAt(LocalDateTime.now());
            versement.setUser(user);
            versementRepo.save(versement);

            return "DELETED";

        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
