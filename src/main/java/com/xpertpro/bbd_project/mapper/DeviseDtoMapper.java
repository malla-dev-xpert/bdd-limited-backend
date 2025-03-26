package com.xpertpro.bbd_project.mapper;

import com.xpertpro.bbd_project.dto.devises.DeviseDto;
import com.xpertpro.bbd_project.entity.Devises;
import org.springframework.stereotype.Component;

@Component
public class DeviseDtoMapper {
    public Devises toEntity(DeviseDto dto) {
        Devises devises = new Devises();
        devises.setCode(dto.getCode());
        devises.setName(dto.getName());
        devises.setRate(dto.getRate());
        return devises;
    }
}
