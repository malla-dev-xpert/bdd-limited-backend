package com.xpertpro.bbd_project.dtoMapper;

import com.xpertpro.bbd_project.dto.containers.ContainersDto;
import com.xpertpro.bbd_project.entity.Containers;
import org.springframework.stereotype.Component;

@Component
public class ContainersDtoMapper {
    public Containers toEntity(ContainersDto dto) {
        Containers containers = new Containers();
        containers.setReference(dto.getReference());
        containers.setIsAvailable(dto.getIsAvailable());
        containers.setLoadingFee(dto.getLoadingFee());
        containers.setLocalCharge(dto.getLocalCharge());
        containers.setLocationFee(dto.getLocationFee());
        containers.setOverweightFee(dto.getOverweightFee());
        containers.setCheckingFee(dto.getCheckingFee());
        containers.setTelxFee(dto.getTelxFee());
        containers.setOtherFees(dto.getOtherFees());
        containers.setMargin(dto.getMargin());
        return containers;
    }
}
