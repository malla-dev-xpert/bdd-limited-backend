package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.partners.CreatePartnersDto;
import com.xpertpro.bbd_project.dto.partners.UpdatePartnersDto;
import com.xpertpro.bbd_project.entity.Partners;
import com.xpertpro.bbd_project.entity.RolesEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.dtoMapper.PartnersDtoMapper;
import com.xpertpro.bbd_project.repository.PartnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PartnerServices {
    @Autowired
    PartnerRepository partnerRepository;
    @Autowired
    PartnersDtoMapper partnersDtoMapper;

    public String createPartners(CreatePartnersDto partnersDto) {

        if (partnerRepository.findByEmail(partnersDto.getEmail()).isPresent()) {
            return "EMAIL_EXIST";
        }

        if (partnerRepository.findByPhoneNumber(partnersDto.getPhoneNumber()).isPresent()) {
            return "PHONE_EXIST";
        }

        Partners partners = partnersDtoMapper.toEntity(partnersDto);

        partnerRepository.save(partners);
        return "SUCCESS";
    }

    public Page<Partners> getAllPartners(int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").ascending());
        return partnerRepository.findByStatus(StatusEnum.CREATE, pageable);
    }

    public Page<Partners> findPartnersByType(int page, String type) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").ascending());
        return partnerRepository.findByAccountType(type, pageable);
    }

    public String deletePartners(Long id) {
        Partners partners = partnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partners not found with ID: " + id));

        partners.setStatus(StatusEnum.DELETE);
        partnerRepository.save(partners);
        return "Partner deleted successfully";
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
