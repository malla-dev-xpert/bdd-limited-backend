package com.xpertpro.bbd_project.dtoMapper;

import com.xpertpro.bbd_project.dto.warehouse.WarehouseDto;
import com.xpertpro.bbd_project.entity.Warehouse;
import org.springframework.stereotype.Component;

@Component
public class WarehouseDtoMapper {
    public Warehouse toEntity(WarehouseDto dto) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(dto.getName());
        warehouse.setAdresse(dto.getAdresse());
        warehouse.setStorageType(dto.getStorageType());
        return warehouse;
    }
}
