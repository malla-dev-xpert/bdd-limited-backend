package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.PackageDto;
import com.xpertpro.bbd_project.dto.achats.AchatDto;
import com.xpertpro.bbd_project.dto.achats.LigneAchatDto;
import com.xpertpro.bbd_project.dto.achats.VersementDto;
import com.xpertpro.bbd_project.dto.partners.PartnerDto;
import com.xpertpro.bbd_project.dto.partners.UpdatePartnersDto;
import com.xpertpro.bbd_project.entity.Partners;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.dtoMapper.PartnersDtoMapper;
import com.xpertpro.bbd_project.repository.PackageRepository;
import com.xpertpro.bbd_project.repository.PartnerRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
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
public class PartnerServices {
    @Autowired
    PartnerRepository partnerRepository;
    @Autowired
    PartnersDtoMapper partnersDtoMapper;
    @Autowired
    PackageRepository packageRepository;
    @Autowired
    UserRepository userRepository;

    public String createPartners(PartnerDto partnersDto, Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (partnerRepository.findByEmail(partnersDto.getEmail()).isPresent()) {
            return "EMAIL_EXIST";
        }

        if (partnerRepository.findByPhoneNumber(partnersDto.getPhoneNumber()).isPresent()) {
            return "PHONE_EXIST";
        }

        Partners partners = partnersDtoMapper.toEntity(partnersDto);
        partners.setUser(user);

        partnerRepository.save(partners);
        return "SUCCESS";
    }

    public List<PartnerDto> getAllCustomer(int page, String query) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Partners> partners = partnerRepository.findByStatus(StatusEnum.CREATE, pageable);

        if (query != null && !query.isEmpty()) {
            partners = partnerRepository.findByStatusAndSearchQuery(
                    StatusEnum.CREATE,
                    "%" + query.toLowerCase() + "%",
                    pageable
            );
        } else {
            partners = partnerRepository.findByStatus(StatusEnum.CREATE, pageable);
        }


        return partners.stream()
                .filter(partner -> partner.getStatus() != StatusEnum.DELETE)
                .filter(partner -> partner.getAccountType().equals("CLIENT"))
                .sorted(Comparator.comparing(Partners::getCreatedAt).reversed())
                .map(partner -> {
                    PartnerDto dto = new PartnerDto();
                    dto.setId(partner.getId());
                    dto.setAccountType(partner.getAccountType());
                    dto.setAdresse(partner.getAdresse());
                    dto.setCountry(partner.getCountry());
                    dto.setEmail(partner.getEmail());
                    dto.setFirstName(partner.getFirstName());
                    dto.setLastName(partner.getLastName());
                    dto.setPhoneNumber(partner.getPhoneNumber());
                    dto.setBalance(partner.getBalance());

                    List<VersementDto> versementDtoList = partner.getVersements().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .map(v -> {
                                VersementDto newVersemtDto = new VersementDto();
                                newVersemtDto.setId(v.getId());
                                newVersemtDto.setMontantVerser(v.getMontantVerser());
                                newVersemtDto.setMontantRestant(v.getMontantRestant());
                                newVersemtDto.setPartnerPhone(v.getPartner() != null ? v.getPartner().getPhoneNumber() : null);
                                newVersemtDto.setPartnerId(v.getPartner() != null ? v.getPartner().getId() : null);
                                newVersemtDto.setPartnerCountry(v.getPartner() != null ? v.getPartner().getCountry() : null);
                                newVersemtDto.setPartnerName(v.getPartner() != null ? v.getPartner().getFirstName()  + " " + v.getPartner().getLastName() : null);
                                newVersemtDto.setPartnerAccountType(v.getPartner() != null ? v.getPartner().getAccountType() : null);
                                newVersemtDto.setCreatedAt(v.getCreatedAt());
                                newVersemtDto.setReference(v.getReference());
                                newVersemtDto.setCommissionnairePhone(v.getCommissionnairePhone());
                                newVersemtDto.setCommissionnaireName(v.getCommissionnaireName());
                                newVersemtDto.setDeviseId(v.getDevise() != null
                                        ? v.getDevise().getId()
                                        : null);
                                newVersemtDto.setDeviseCode(v.getDevise() != null
                                        ? v.getDevise().getCode()
                                        : null);

                                List<AchatDto> achatDtos = v.getAchats().stream()
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
                                            achatDto.setMontantRestant(v.getMontantRestant());
                                            achatDto.setMontantVerser(v.getMontantVerser());
                                            achatDto.setReferenceVersement(v.getReference());

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

                                newVersemtDto.setAchats(achatDtos);

                                return newVersemtDto;

                            }).collect(Collectors.toList());

                    List<PackageDto> packageDtoList = partner.getPackages().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .map(e -> {
                                PackageDto newPackageDto = new PackageDto();
                                newPackageDto.setItemQuantity(e.getItemQuantity());
                                newPackageDto.setWeight(e.getWeight());
                                newPackageDto.setClientName(e.getClient() != null ? e.getClient().getFirstName() + " " + e.getClient().getLastName() : null);
                                newPackageDto.setClientPhone(e.getClient() != null ? e.getClient().getPhoneNumber() : null);
                                newPackageDto.setExpeditionType(e.getExpeditionType());
                                newPackageDto.setRef(e.getRef());
                                newPackageDto.setCbn(e.getCbn());
                                newPackageDto.setDestinationCountry(e.getDestinationCountry());
                                newPackageDto.setStartCountry(e.getStartCountry());
                                newPackageDto.setStartDate(e.getStartDate());
                                newPackageDto.setArrivalDate(e.getArrivalDate());
                                newPackageDto.setId(e.getId());
                                newPackageDto.setStatus(e.getStatus().name());

                                return newPackageDto;

                            }).collect(Collectors.toList());

                    dto.setVersements(versementDtoList);
                    dto.setPackages(packageDtoList);

                    return dto;
                })
                .collect(Collectors.toList());

    }

    public List<PartnerDto> getAllSupplier(int page, String query) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Partners> partners = partnerRepository.findByStatus(StatusEnum.CREATE, pageable);

        if (query != null && !query.isEmpty()) {
            partners = partnerRepository.findByStatusAndSearchQuery(
                    StatusEnum.CREATE,
                    "%" + query.toLowerCase() + "%",
                    pageable
            );
        } else {
            partners = partnerRepository.findByStatus(StatusEnum.CREATE, pageable);
        }


        return partners.stream()
                .filter(partner -> partner.getStatus() != StatusEnum.DELETE)
                .filter(partner -> partner.getAccountType().equals("FOURNISSEUR"))
                .sorted(Comparator.comparing(Partners::getCreatedAt).reversed())
                .map(partner -> {
                    PartnerDto dto = new PartnerDto();
                    dto.setId(partner.getId());
                    dto.setAccountType(partner.getAccountType());
                    dto.setAdresse(partner.getAdresse());
                    dto.setCountry(partner.getCountry());
                    dto.setEmail(partner.getEmail());
                    dto.setFirstName(partner.getFirstName());
                    dto.setLastName(partner.getLastName());
                    dto.setPhoneNumber(partner.getPhoneNumber());
                    dto.setBalance(partner.getBalance());

                    List<VersementDto> versementDtoList = partner.getVersements().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .map(v -> {
                                VersementDto newVersemtDto = new VersementDto();
                                newVersemtDto.setId(v.getId());
                                newVersemtDto.setMontantVerser(v.getMontantVerser());
                                newVersemtDto.setMontantRestant(v.getMontantRestant());
                                newVersemtDto.setPartnerPhone(v.getPartner() != null ? v.getPartner().getPhoneNumber() : null);
                                newVersemtDto.setPartnerId(v.getPartner() != null ? v.getPartner().getId() : null);
                                newVersemtDto.setPartnerCountry(v.getPartner() != null ? v.getPartner().getCountry() : null);
                                newVersemtDto.setPartnerName(v.getPartner() != null ? v.getPartner().getFirstName()  + " " + v.getPartner().getLastName() : null);
                                newVersemtDto.setPartnerAccountType(v.getPartner() != null ? v.getPartner().getAccountType() : null);
                                newVersemtDto.setCreatedAt(v.getCreatedAt());
                                newVersemtDto.setReference(v.getReference());
                                newVersemtDto.setCommissionnairePhone(v.getCommissionnairePhone());
                                newVersemtDto.setCommissionnaireName(v.getCommissionnaireName());
                                newVersemtDto.setDeviseId(v.getDevise() != null
                                        ? v.getDevise().getId()
                                        : null);
                                newVersemtDto.setDeviseCode(v.getDevise() != null
                                        ? v.getDevise().getCode()
                                        : null);

                                List<AchatDto> achatDtos = v.getAchats().stream()
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
                                            achatDto.setMontantRestant(v.getMontantRestant());
                                            achatDto.setMontantVerser(v.getMontantVerser());
                                            achatDto.setReferenceVersement(v.getReference());

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

                                newVersemtDto.setAchats(achatDtos);

                                return newVersemtDto;

                            }).collect(Collectors.toList());

                    List<PackageDto> packageDtoList = partner.getPackages().stream()
                            .filter(item -> item.getStatus() != StatusEnum.DELETE)
                            .map(e -> {
                                PackageDto newPackageDto = new PackageDto();
                                newPackageDto.setItemQuantity(e.getItemQuantity());
                                newPackageDto.setWeight(e.getWeight());
                                newPackageDto.setClientName(e.getClient() != null ? e.getClient().getFirstName() + " " + e.getClient().getLastName() : null);
                                newPackageDto.setClientPhone(e.getClient() != null ? e.getClient().getPhoneNumber() : null);
                                newPackageDto.setExpeditionType(e.getExpeditionType());
                                newPackageDto.setRef(e.getRef());
                                newPackageDto.setCbn(e.getCbn());
                                newPackageDto.setDestinationCountry(e.getDestinationCountry());
                                newPackageDto.setStartCountry(e.getStartCountry());
                                newPackageDto.setStartDate(e.getStartDate());
                                newPackageDto.setArrivalDate(e.getArrivalDate());
                                newPackageDto.setId(e.getId());
                                newPackageDto.setStatus(e.getStatus().name());

                                return newPackageDto;

                            }).collect(Collectors.toList());

                    dto.setVersements(versementDtoList);
                    dto.setPackages(packageDtoList);

                    return dto;
                })
                .collect(Collectors.toList());

    }

    public Page<PartnerDto> findPartnersByType(int page, String type, String query) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<Partners> partners = partnerRepository.findByAccountType(type, PageRequest.of(page, 50));
        if (query != null && !query.isEmpty()) {
            partners = partnerRepository.findByStatusAndSearchQuery(
                    StatusEnum.CREATE,
                    "%" + query.toLowerCase() + "%",
                    pageable
            );
        } else {
            partners = partnerRepository.findByStatus(StatusEnum.CREATE, pageable);
        }

        return partners
                .map(partner -> new PartnerDto(
                partner.getId(),
                partner.getFirstName(),
                partner.getLastName(),
                partner.getPhoneNumber(),
                partner.getEmail(),
                partner.getCountry(),
                partner.getAdresse(),
                partner.getAccountType(),
                partner.getBalance(),
                partner.getVersements().stream().map(versements -> {
                    VersementDto newDto = new VersementDto();
                    newDto.setId(versements.getId());
                    return newDto;
                }).collect(Collectors.toList()),
                partner.getPackages().stream().map(expeditions -> {
                    PackageDto newDto = new PackageDto();
                    newDto.setId(expeditions.getId());
                    return newDto;
                }).collect(Collectors.toList())
                )
        );
    }

    public String deletePartner(Long clientId, Long userId) {
        Optional<Partners> optionalPartner = partnerRepository.findById(clientId);
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (optionalPartner.isEmpty()) {
            return "PARTNER_NOT_FOUND";
        }

        Partners partner = optionalPartner.get();

        boolean hasPackages = packageRepository.existsByClientId(clientId);
        if (hasPackages) {
            return "PACKAGE_FOUND";
        }

        partner.setStatus(StatusEnum.DELETE);
        partner.setEditedAt(LocalDateTime.now());
        partner.setUser(user);
        partnerRepository.save(partner);

        return "DELETED";
    }

    public UpdatePartnersDto updatePartnersDto(Long id, UpdatePartnersDto updatePartnersDto) {
        Optional<Partners> optionalPartners = partnerRepository.findById(id);

        if (optionalPartners.isPresent()) {
            Partners partners = optionalPartners.get();

            if (updatePartnersDto.getFirstName() != null) partners.setFirstName(updatePartnersDto.getFirstName());
            if (updatePartnersDto.getLastName() != null) partners.setLastName(updatePartnersDto.getLastName());
            if (updatePartnersDto.getPhoneNumber() != null) partners.setPhoneNumber(updatePartnersDto.getPhoneNumber());
            if (updatePartnersDto.getEmail() != null) partners.setEmail(updatePartnersDto.getEmail());
            if (updatePartnersDto.getCountry() != null) partners.setCountry(updatePartnersDto.getCountry());
            if (updatePartnersDto.getAdresse() != null) partners.setAdresse(updatePartnersDto.getAdresse());
            if (updatePartnersDto.getAccountType() != null) partners.setAccountType(updatePartnersDto.getAccountType());
            partners.setEditedAt(updatePartnersDto.getEditedAt());

            partnerRepository.save(partners);
            return updatePartnersDto;
        } else {
            throw new RuntimeException("Partners not found with ID: " + id);
        }
    }

    public Partners getPartnerById(Long id) {
        Optional<Partners> optionalPartners = partnerRepository.findById(id);
        if (optionalPartners.isPresent()) {
            Partners partners = optionalPartners.get();
            return partners;
        } else {
            throw new RuntimeException("Partenaire non trouvé avec l'ID : " + id);
        }
    }

}
