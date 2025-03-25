package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.partners.CreatePartnersDto;
import com.xpertpro.bbd_project.entity.Partners;
import com.xpertpro.bbd_project.entity.RolesEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.mapper.PartnersDtoMapper;
import com.xpertpro.bbd_project.repository.PartnerRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

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
//
//    public List<Partenaire> obtenirTousLesPartenaires() {
//        return partenaireRepository.findAll();
//    }
//
//    public List<Partenaire> obtenirParType(TypeCompte typeCompte) {
//        return partenaireRepository.findByTypeCompte(typeCompte);
//    }
//
//    public void supprimerPartenaire(Long id) {
//        partenaireRepository.deleteById(id);
//    }
}
