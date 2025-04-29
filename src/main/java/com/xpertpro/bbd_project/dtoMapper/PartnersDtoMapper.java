package com.xpertpro.bbd_project.dtoMapper;

import com.xpertpro.bbd_project.dto.partners.PartnerDto;
import com.xpertpro.bbd_project.entity.Partners;
import org.springframework.stereotype.Component;

@Component
public class PartnersDtoMapper {
    public Partners toEntity(PartnerDto dto) {
        Partners partners = new Partners();
        partners.setCountry(dto.getCountry());
        partners.setEmail(dto.getEmail());
        partners.setPhoneNumber(dto.getPhoneNumber());
        partners.setFirstName(dto.getFirstName());
        partners.setLastName(dto.getLastName());
        partners.setAdresse(dto.getAdresse());
        partners.setAccountType(dto.getAccountType());
        return partners;
    }
}
