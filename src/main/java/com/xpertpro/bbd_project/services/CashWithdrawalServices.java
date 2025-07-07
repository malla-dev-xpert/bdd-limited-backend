package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.CashWithdrawalDto;
import com.xpertpro.bbd_project.entity.*;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class CashWithdrawalServices {

    private final PartnerRepository partnerRepository;
    private final VersementRepo versementRepo;
    private final DevisesRepository devisesRepository;
    private final UserRepository userRepository;
    private final CashWithdrawalRepository cashWithdrawalRepository;

    public CashWithdrawalServices(PartnerRepository partnerRepository,
                                  VersementRepo versementRepo,
                                 DevisesRepository devisesRepository,
                                 UserRepository userRepository,
                                 CashWithdrawalRepository cashWithdrawalRepository) {
        this.partnerRepository = partnerRepository;
        this.versementRepo = versementRepo;
        this.devisesRepository = devisesRepository;
        this.userRepository = userRepository;
        this.cashWithdrawalRepository = cashWithdrawalRepository;
    }

    @Transactional
    public String createRetrait(CashWithdrawalDto dto) {
        // Validation des entrées
        validateInput(dto);

        // Récupération des entités avec gestion d'erreur spécifique
        Partners partner = partnerRepository.findById(dto.getPartnerId())
                .orElseThrow(() -> new EntityNotFoundException("Client introuvable avec l'ID: " + dto.getPartnerId()));

        Versements versement = versementRepo.findById(dto.getVersementId())
                .orElseThrow(() -> new EntityNotFoundException("Versement introuvable avec l'ID: " + dto.getVersementId()));

        Devises devise = devisesRepository.findById(dto.getDeviseId())
                .orElseThrow(() -> new EntityNotFoundException("Devise introuvable avec l'ID: " + dto.getDeviseId()));

        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable avec l'ID: " + dto.getUserId()));

        // Validation métier
        validateBusinessRules(dto, partner, versement);

        // Mise à jour des entités
        updateEntities(dto, partner, versement);

        // Création et sauvegarde du retrait
        CashWithdrawal retrait = createCashWithdrawal(dto, partner, versement, devise, user);
        cashWithdrawalRepository.save(retrait);

        return "SUCCESS";
    }

    private void validateInput(CashWithdrawalDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Les données du retrait ne peuvent pas être nulles");
        }
        if (dto.getMontant() <= 0) {
            throw new IllegalArgumentException("Le montant doit être supérieur à zéro");
        }
        if (!StringUtils.hasText(dto.getNote())) {
            throw new IllegalArgumentException("Une note est requise pour le retrait");
        }
    }

    private void validateBusinessRules(CashWithdrawalDto dto, Partners partner, Versements versement) {
        if (!versement.getPartner().getId().equals(partner.getId())) {
            throw new IllegalArgumentException("Le versement ne correspond pas au client sélectionné");
        }

        if (dto.getMontant() > versement.getMontantRestant()) {
            throw new IllegalArgumentException("Montant supérieur au montant restant du versement");
        }

        if (dto.getMontant() > partner.getBalance()) {
            throw new IllegalArgumentException("Solde client insuffisant pour effectuer le retrait");
        }
    }

    private void updateEntities(CashWithdrawalDto dto, Partners partner, Versements versement) {
        // Maj du montant restant du versement
        versement.setMontantRestant(versement.getMontantRestant() - dto.getMontant());
        versementRepo.save(versement);

        // Maj du solde client
        partner.setBalance(partner.getBalance() - dto.getMontant());
        partnerRepository.save(partner);
    }

    private CashWithdrawal createCashWithdrawal(CashWithdrawalDto dto, Partners partner,
                                                Versements versement, Devises devise, UserEntity user) {
        CashWithdrawal retrait = new CashWithdrawal();
        retrait.setMontant(dto.getMontant());
        retrait.setPartner(partner);
        retrait.setVersement(versement);
        retrait.setDevise(devise);
        retrait.setUser(user);
        retrait.setNote(dto.getNote());
        retrait.setDateRetrait(LocalDateTime.now());
        retrait.setStatus(StatusEnum.CREATE);
        return retrait;
    }
}