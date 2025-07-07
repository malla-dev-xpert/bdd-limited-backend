package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.CashWithdrawalDto;
import com.xpertpro.bbd_project.dto.achats.AchatDto;
import com.xpertpro.bbd_project.dto.achats.VersementDto;
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
    @Autowired
    private DevisesRepository devisesRepository;

    @Autowired
    private LogServices logServices;
    @Autowired
    private ExchangeRateServices exchangeRateServices;
    @Autowired
    ExchangeRateRepository exchangeRateRepository;

    @Transactional
    public String newVersement(Long userId, Long partnerId, Long deviseId, VersementDto dto) {
        // Vérification des entités existantes
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Partners partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partenaire introuvable"));

        Devises deviseOrigine = devisesRepository.findById(deviseId)
                .orElseThrow(() -> new RuntimeException("Devise introuvable"));

        // 1. Vérifier si la devise CNY existe, sinon la créer automatiquement
        Devises deviseReference = devisesRepository.findByCode("CNY")
                .orElseGet(() -> {
                    Devises newDevise = new Devises();
                    newDevise.setCode("CNY");
                    newDevise.setName("Dollar américain");
                    newDevise.setUser(user);
                    newDevise.setCreatedAt(LocalDateTime.now());
                    return devisesRepository.save(newDevise);
                });

        // 2. Calcul du taux de change
        Double tauxVersement = 1.0; // Taux par défaut si même devise

        if (!deviseOrigine.getCode().equals("CNY")) {
            try {
                // Inversez le taux pour obtenir deviseOrigine->CNY
                tauxVersement = 1 / exchangeRateServices.getRealTimeRate("CNY", deviseOrigine.getCode());

                // Sauvegarder le taux utilisé
                ExchangeRate exchangeRate = new ExchangeRate();
                exchangeRate.setFromDevise(deviseOrigine);
                exchangeRate.setToDevise(deviseReference);
                exchangeRate.setRate(tauxVersement);
                exchangeRate.setTimestamp(LocalDateTime.now());
                exchangeRateRepository.save(exchangeRate);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la récupération du taux de change", e);
            }
        }

        // 3. Création du versement
        Versements newVersement = new Versements();
        newVersement.setMontantVerser(dto.getMontantVerser());
        newVersement.setMontantRestant(dto.getMontantVerser());
        newVersement.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
        newVersement.setUser(user);
        newVersement.setPartner(partner);
        newVersement.setCommissionnaireName(dto.getCommissionnaireName());
        newVersement.setCommissionnairePhone(dto.getCommissionnairePhone());
        newVersement.setStatus(StatusEnum.CREATE);
        newVersement.setDevise(deviseOrigine);
        newVersement.setTauxUtilise(tauxVersement);
        newVersement.setType(dto.getType());
        newVersement.setNote(dto.getNote());

        // 4. Conversion en USD pour le solde du partenaire
        Double montantEnUSD = dto.getMontantVerser() * tauxVersement;

        // 5. Mise à jour du solde du partenaire (toujours en USD)
        partner.setBalance(partner.getBalance() + montantEnUSD);
        partnerRepository.save(partner);

        // 6. Sauvegarde et génération de référence
        versementRepo.save(newVersement);
        String ref = String.format("BBDPAY-%02d", newVersement.getId());
        newVersement.setReference(ref);
        Versements v = versementRepo.save(newVersement);

        logServices.logAction(user, "NEW_VERSEMENT", "Versement", v.getId());

        return ref;
    }

    public List<VersementDto> getAll(int page) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Versements> versements = versementRepo.findByStatusNot(StatusEnum.DELETE, pageable);

        return versements.stream()
                .filter(versement -> versement.getStatus() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(Versements::getCreatedAt).reversed())
                .map(versement -> {
                    VersementDto dto = new VersementDto();
                    dto.setId(versement.getId());
                    dto.setReference(versement.getReference());
                    dto.setMontantVerser(versement.getMontantVerser());
                    dto.setMontantRestant(versement.getMontantRestant()); // Récupération directe depuis le versement
                    dto.setCreatedAt(versement.getCreatedAt());
                    dto.setEditedAt(versement.getEditedAt());
                    dto.setCommissionnairePhone(versement.getCommissionnairePhone());
                    dto.setCommissionnaireName(versement.getCommissionnaireName());
                    dto.setType(versement.getType());
                    dto.setNote(versement.getNote());
                    dto.setPartnerId(versement.getPartner() != null
                            ? versement.getPartner().getId()
                            : null);
                    dto.setPartnerName(versement.getPartner() != null
                            ? versement.getPartner().getFirstName() + " " + versement.getPartner().getLastName()
                            : null);
                    dto.setPartnerAccountType(versement.getPartner() != null
                            ? versement.getPartner().getAccountType()
                            : null);
                    dto.setPartnerPhone(versement.getPartner() != null
                            ? versement.getPartner().getPhoneNumber()
                            : null);
                    dto.setPartnerCountry(versement.getPartner() != null
                            ? versement.getPartner().getCountry()
                            : null);
                    dto.setDeviseId(versement.getDevise() != null
                            ? versement.getDevise().getId()
                            : null);
                    dto.setDeviseCode(versement.getDevise() != null
                            ? versement.getDevise().getCode()
                            : null);

//                    la liste des achats dans le verment
                    List<AchatDto> achatDtos = versement.getAchats().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .map(item -> {
                                AchatDto achatDto = new AchatDto();
                                achatDto.setId(item.getId());
                                achatDto.setMontantTotal(item.getMontantTotal());
                                achatDto.setCreatedAt(item.getCreatedAt() != null ? item.getCreatedAt() : null);
                                // Utilisation des montants du versement parent
                                achatDto.setReferenceVersement(item.getVersement().getReference());
                                achatDto.setIsDebt(item.getIsDebt());

                                List<ItemDto> itemsDtos = item.getItems().stream()
                                        .filter(items -> items.getStatus() != StatusEnum.DELETE)
                                        .map(i -> {
                                            ItemDto itemDto = new ItemDto();
                                            itemDto.setId(i.getId());
                                            itemDto.setDescription(i.getDescription());
                                            itemDto.setQuantity(i.getQuantity());
                                            itemDto.setUnitPrice(i.getUnitPrice());
                                            itemDto.setSupplierName(i.getSupplier() != null ? i.getSupplier().getFirstName() + " " + i.getSupplier().getLastName() : null);
                                            itemDto.setSupplierPhone(i.getSupplier() != null ? i.getSupplier().getPhoneNumber() : null);
                                            itemDto.setStatus(i.getStatus().name());
                                            itemDto.setTotalPrice(i.getTotalPrice());
                                            itemDto.setSalesRate(i.getSalesRate());
                                            return itemDto;
                                        }).collect(Collectors.toList());

                                achatDto.setItems(itemsDtos);

                                return achatDto;
                            }).collect(Collectors.toList());

                    dto.setAchats(achatDtos);

//                    La liste des retraits d'argennt
                    List<CashWithdrawalDto> cashWithdrawalDtos = versement.getCashWithdrawalList().stream()
                            .filter(cashWithdrawal -> cashWithdrawal.getStatus() != StatusEnum.DELETE)
                            .map(cashWithdrawal -> {
                                CashWithdrawalDto cashWithdrawalDto = new CashWithdrawalDto();
                                cashWithdrawalDto.setId(cashWithdrawal.getId());
                                cashWithdrawalDto.setMontant(cashWithdrawal.getMontant());
                                cashWithdrawalDto.setNote(cashWithdrawal.getNote());
                                cashWithdrawalDto.setDateRetrait(cashWithdrawal.getDateRetrait());
                                cashWithdrawalDto.setVersementId(cashWithdrawal.getVersement() != null ? cashWithdrawal.getVersement().getId() : null);
                                cashWithdrawalDto.setUserId(cashWithdrawal.getUser() != null ? cashWithdrawal.getUser().getId() : null);
                                cashWithdrawalDto.setDeviseId(cashWithdrawal.getDevise() != null ? cashWithdrawal.getDevise().getId() : null);
                                cashWithdrawalDto.setPartnerId(cashWithdrawal.getPartner() != null ? cashWithdrawal.getPartner().getId() : null);
                                cashWithdrawalDto.setUserName(cashWithdrawal.getUser() != null ? cashWithdrawal.getUser().getFirstName() + " " + cashWithdrawal.getUser().getLastName() : null);

                                return cashWithdrawalDto;
                            }).collect(Collectors.toList());

                    dto.setCashWithdrawalDtoList(cashWithdrawalDtos);

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
                .filter(versement -> versement.getStatus() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(Versements::getCreatedAt).reversed())
                .map(versement -> {
                    VersementDto dto = new VersementDto();
                    dto.setId(versement.getId());
                    dto.setReference(versement.getReference());
                    dto.setMontantRestant(versement.getMontantRestant());
                    dto.setMontantVerser(versement.getMontantVerser());
                    dto.setCreatedAt(versement.getCreatedAt());
                    dto.setEditedAt(versement.getEditedAt());
                    dto.setCommissionnairePhone(versement.getCommissionnairePhone());
                    dto.setCommissionnaireName(versement.getCommissionnaireName());
                    dto.setType(versement.getType());
                    dto.setNote(versement.getNote());
                    dto.setDeviseId(versement.getDevise() != null
                            ? versement.getDevise().getId()
                            : null);
                    dto.setDeviseCode(versement.getDevise() != null
                            ? versement.getDevise().getCode()
                            : null);

                    // Info partenaire (client)
                    if (versement.getPartner() != null) {
                        dto.setPartnerName(versement.getPartner().getFirstName() + " " + versement.getPartner().getLastName());
                        dto.setPartnerPhone(versement.getPartner().getPhoneNumber());
                        dto.setPartnerCountry(versement.getPartner().getCountry());
                        dto.setPartnerId(versement.getPartner().getId());
                    }

                    // Liste des achats associés
                    List<AchatDto> achatDtos = versement.getAchats().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .map(item -> {
                                AchatDto achatDto = new AchatDto();
                                achatDto.setId(item.getId());
                                achatDto.setMontantTotal(item.getMontantTotal());
                                achatDto.setCreatedAt(item.getCreatedAt() != null ? item.getCreatedAt() : null);
                                // Info versement
                                if (item.getVersement() != null) {
                                    achatDto.setReferenceVersement(item.getVersement().getReference());
                                }
                                achatDto.setIsDebt(item.getIsDebt());

                                // Lignes d'achat
                                List<ItemDto> itemsDtos = item.getItems().stream()
                                        .filter(items -> items.getStatus() != StatusEnum.DELETE)
                                        .map(i -> {
                                            ItemDto itemDto = new ItemDto();
                                            itemDto.setId(i.getId());
                                            itemDto.setDescription(i.getDescription());
                                            itemDto.setQuantity(i.getQuantity());
                                            itemDto.setUnitPrice(i.getUnitPrice());
                                            itemDto.setSupplierName(i.getSupplier() != null ? i.getSupplier().getFirstName() + " " + i.getSupplier().getLastName() : null);
                                            itemDto.setSupplierPhone(i.getSupplier() != null ? i.getSupplier().getPhoneNumber() : null);
                                            itemDto.setStatus(i.getStatus().name());
                                            itemDto.setTotalPrice(i.getTotalPrice());
                                            itemDto.setSalesRate(i.getSalesRate());
                                            return itemDto;
                                        }).collect(Collectors.toList());

                                achatDto.setItems(itemsDtos);
                                return achatDto;
                            }).collect(Collectors.toList());

                    dto.setAchats(achatDtos);

                    //La liste des retraits d'argennt
                    List<CashWithdrawalDto> cashWithdrawalDtos = versement.getCashWithdrawalList().stream()
                            .filter(cashWithdrawal -> cashWithdrawal.getStatus() != StatusEnum.DELETE)
                            .map(cashWithdrawal -> {
                                CashWithdrawalDto cashWithdrawalDto = new CashWithdrawalDto();
                                cashWithdrawalDto.setId(cashWithdrawal.getId());
                                cashWithdrawalDto.setMontant(cashWithdrawal.getMontant());
                                cashWithdrawalDto.setNote(cashWithdrawal.getNote());
                                cashWithdrawalDto.setDateRetrait(cashWithdrawal.getDateRetrait());
                                cashWithdrawalDto.setVersementId(cashWithdrawal.getVersement() != null ? cashWithdrawal.getVersement().getId() : null);
                                cashWithdrawalDto.setUserId(cashWithdrawal.getUser() != null ? cashWithdrawal.getUser().getId() : null);
                                cashWithdrawalDto.setDeviseId(cashWithdrawal.getDevise() != null ? cashWithdrawal.getDevise().getId() : null);
                                cashWithdrawalDto.setPartnerId(cashWithdrawal.getPartner() != null ? cashWithdrawal.getPartner().getId() : null);
                                cashWithdrawalDto.setUserName(cashWithdrawal.getUser() != null ? cashWithdrawal.getUser().getFirstName() + " " + cashWithdrawal.getUser().getLastName() : null);

                                return cashWithdrawalDto;
                            }).collect(Collectors.toList());

                    dto.setCashWithdrawalDtoList(cashWithdrawalDtos);
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
        Optional.ofNullable(dto.getCommissionnairePhone()).ifPresent(versement::setCommissionnairePhone);
        Optional.ofNullable(dto.getCommissionnaireName()).ifPresent(versement::setCommissionnaireName);

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
