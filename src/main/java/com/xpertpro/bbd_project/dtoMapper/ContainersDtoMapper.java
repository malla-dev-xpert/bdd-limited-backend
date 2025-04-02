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
        return containers;
    }
}
