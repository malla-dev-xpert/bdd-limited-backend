package com.xpertpro.bbd_project.dtoMapper;

import com.xpertpro.bbd_project.dto.PackageDto;
import com.xpertpro.bbd_project.entity.Packages;
import org.springframework.stereotype.Component;

@Component
public class PackageDtoMapper {
    public Packages toEntity(PackageDto dto) {
        Packages packages = new Packages();
        packages.setExpeditionType(dto.getExpeditionType());
        packages.setArrivalDate(dto.getArrivalDate());
        packages.setCbn(dto.getCbn());
        packages.setDestinationCountry(dto.getDestinationCountry());
        packages.setStartCountry(dto.getStartCountry());
        packages.setItemQuantity(dto.getItemQuantity());
        packages.setStartDate(dto.getStartDate());
        packages.setWeight(dto.getWeight());
        packages.setRef(dto.getRef());
        return packages;
    }
}
