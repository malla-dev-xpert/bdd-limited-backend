package com.xpertpro.bbd_project.mapper;

import com.xpertpro.bbd_project.dto.harbor.HarborDto;
import com.xpertpro.bbd_project.entity.Harbor;
import org.springframework.stereotype.Component;

@Component
public class HarborDtoMapper {
    public Harbor toEntity(HarborDto dto) {
        Harbor harbor = new Harbor();
        harbor.setLocation(dto.getLocation());
        harbor.setName(dto.getName());
        return harbor;
    }
}
