package com.xpertpro.bbd_project.dtoMapper;

import com.xpertpro.bbd_project.dto.ExpeditionDto;
import com.xpertpro.bbd_project.entity.Expeditions;
import org.springframework.stereotype.Component;

@Component
public class ExpeditionDtoMapper {
    public Expeditions toEntity(ExpeditionDto dto) {
        Expeditions expeditions = new Expeditions();
        expeditions.setExpeditionType(dto.getExpeditionType());
        expeditions.setArrivalDate(dto.getArrivalDate());
        expeditions.setCbn(dto.getCbn());
        expeditions.setDestinationCountry(dto.getDestinationCountry());
        expeditions.setStartCountry(dto.getStartCountry());
        expeditions.setStartDate(dto.getStartDate());
        expeditions.setWeight(dto.getWeight());
        expeditions.setRef(dto.getRef());
        return expeditions;
    }
}
