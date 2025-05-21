package com.xpertpro.bbd_project.services;

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

    public List<PartnerDto> getAllPartner(int page, String query) {
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
                .filter(pkg -> pkg.getStatus() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(Partners::getCreatedAt).reversed())
                .map(pkg -> {
                    PartnerDto dto = new PartnerDto();
                    dto.setId(pkg.getId());
                    dto.setAccountType(pkg.getAccountType());
                    dto.setAdresse(pkg.getAdresse());
                    dto.setCountry(pkg.getCountry());
                    dto.setEmail(pkg.getEmail());
                    dto.setFirstName(pkg.getFirstName());
                    dto.setLastName(pkg.getLastName());
                    dto.setPhoneNumber(pkg.getPhoneNumber());
                    dto.setBalance(pkg.getBalance());

                    return dto;
                })
                .collect(Collectors.toList());

    }

    public Page<PartnerDto> findPartnersByType(int page, String type) {
        Page<Partners> partners = partnerRepository.findByAccountType(type, PageRequest.of(page, 50));
        return partners.map(partner -> new PartnerDto(
                partner.getId(),
                partner.getFirstName(),
                partner.getLastName(),
                partner.getPhoneNumber(),
                partner.getEmail(),
                partner.getCountry(),
                partner.getAdresse(),
                partner.getAccountType(),
                partner.getBalance())

        );
    }

    public String deletePartner(Long partnerId, Long userId) {
        Optional<Partners> optionalPartner = partnerRepository.findById(partnerId);
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (optionalPartner.isEmpty()) {
            return "PARTNER_NOT_FOUND";
        }

        Partners partner = optionalPartner.get();

        boolean hasPackages = packageRepository.existsByPartnerId(partnerId);
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
