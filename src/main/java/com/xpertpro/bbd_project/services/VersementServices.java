package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.achats.AchatDto;
import com.xpertpro.bbd_project.dto.achats.LigneAchatDto;
import com.xpertpro.bbd_project.dto.achats.VersementDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    private final RestTemplate restTemplate;
    public VersementServices(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    private LogServices logServices;
    @Autowired
    ExchangeRateRepository exchangeRateRepository;

    @Transactional
    public String newVersement(Long userId, Long partnerId, Long deviseId, VersementDto dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Partners partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partenaire introuvable"));

        Devises devise = devisesRepository.findById(deviseId)
                .orElseThrow(() -> new RuntimeException("Devise introuvable"));

        // Récupérer le taux de change actuel si la devise n'est pas la devise par défaut
        Double tauxUtilise = 1.0; // Taux par défaut pour la devise de référence
        ExchangeRate exchangeRate = null;
        if (!devise.getCode().equals("USD")) { // USD devise de référence
            tauxUtilise = getRealTimeRate("USD", devise.getCode());
            // Sauvegarder le taux de change utilisé
            exchangeRate = saveExchangeRate("USD", devise.getCode());
        }

        // Créer un nouveau versement
        Versements newVersement = new Versements();
        newVersement.setMontantVerser(dto.getMontantVerser());
        newVersement.setMontantRestant(dto.getMontantVerser());
        newVersement.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDateTime.now());
        newVersement.setUser(user);
        newVersement.setPartner(partner);
        newVersement.setCommissionnaireName(dto.getCommissionnaireName());
        newVersement.setCommissionnairePhone(dto.getCommissionnairePhone());
        newVersement.setStatus(StatusEnum.CREATE);
        newVersement.setDevise(devise);
        newVersement.setTauxUtilise(tauxUtilise);

        // Convertir le montant versé en devise de référence pour le solde du partenaire
        Double montantEnDeviseReference = dto.getMontantVerser() / tauxUtilise;

        // Mettre à jour le solde du partenaire (toujours dans la devise de référence)
        Double nouveauSolde = partner.getBalance() + montantEnDeviseReference;
        partner.setBalance(nouveauSolde);
        partnerRepository.save(partner);

        versementRepo.save(newVersement);

        // Générer une référence
        String ref = String.format("BBDPAY-%02d", newVersement.getId());
        newVersement.setReference(ref);

        Versements v = versementRepo.save(newVersement);

        logServices.logAction(user, "NEW_VERSEMENT", "Versement", v.getId());

        return ref;
    }

    public Double getRealTimeRate(String fromCode, String toCode) {
        String url = String.format("https://open.er-api.com/v6/latest/%s", fromCode);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map body = response.getBody();
            if ("success".equals(body.get("result"))) {
                Map<String, Double> rates = (Map<String, Double>) body.get("rates");
                return rates.get(toCode);
            }
        }
        throw new RuntimeException("Échec de récupération du taux de change.");
    }

    public ExchangeRate saveExchangeRate(String fromCode, String toCode) {
        Double rate = getRealTimeRate(fromCode, toCode);
        Devises from = devisesRepository.findByCode(fromCode).orElseThrow();
        Devises to = devisesRepository.findByCode(toCode).orElseThrow();

        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setFromDevise(from);
        exchangeRate.setToDevise(to);
        exchangeRate.setRate(rate);
        exchangeRate.setTimestamp(LocalDateTime.now());
        return exchangeRateRepository.save(exchangeRate);
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

                    List<AchatDto> achatDtos = versement.getAchats().stream()
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
                                achatDto.setMontantRestant(versement.getMontantRestant());
                                achatDto.setMontantVerser(versement.getMontantVerser());
                                achatDto.setReferenceVersement(versement.getReference());

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
