package com.xpertpro.bbd_project.dtoMapper;

import com.xpertpro.bbd_project.dto.carrier.CarrierDto;
import com.xpertpro.bbd_project.entity.Carriers;
import org.springframework.stereotype.Component;

@Component
public class CarrierDtoMapper {
    public Carriers toEntity(CarrierDto dto) {
        Carriers carriers = new Carriers();
        carriers.setName(dto.getName());
        carriers.setContact(dto.getContact());
        return carriers;
    }
}
