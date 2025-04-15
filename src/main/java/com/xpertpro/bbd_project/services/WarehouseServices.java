package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.warehouse.WarehouseDto;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.entity.Warehouse;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.dtoMapper.WarehouseDtoMapper;
import com.xpertpro.bbd_project.repository.UserRepository;
import com.xpertpro.bbd_project.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class WarehouseServices {
    @Autowired
    WarehouseRepository warehouseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WarehouseDtoMapper warehouseDtoMapper;

    public String createWarehouse(WarehouseDto warehouse, Long userId) {

        if (warehouseRepository.findByName(warehouse.getName()).isPresent()) {
            return "NAME_EXIST";
        }

        if (warehouseRepository.findByAdresse(warehouse.getAdresse()).isPresent()) {
            return "ADRESS_EXIST";
        }

        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Warehouse warehouse1 = warehouseDtoMapper.toEntity(warehouse);

        warehouse1.setCreatedAt(warehouse.getCreatedAt());
        warehouse1.setUser(user);
        warehouse1.setStatus(StatusEnum.CREATE);

        warehouseRepository.save(warehouse1);
        return  "SUCCESS";
    }

    public Page<Warehouse> findAllWarehouse(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        return warehouseRepository.findByStatus(StatusEnum.CREATE, pageable);
    }

    public String updateWarehouse(Long id, WarehouseDto newWarehouse, Long userId) {
        Optional<Warehouse> optionalWarehouse = warehouseRepository.findById(id);
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (warehouseRepository.findByName(newWarehouse.getName()).isPresent()) {
            return "NAME_EXIST";
        }

        if (optionalWarehouse.isPresent() && optionalUser.isPresent()) {
            Warehouse warehouse = optionalWarehouse.get();

            if (newWarehouse.getAdresse() != null) warehouse.setAdresse(newWarehouse.getAdresse());
            if (newWarehouse.getName() != null) warehouse.setName(newWarehouse.getName());
            if (newWarehouse.getStorageType() != null) warehouse.setStorageType(newWarehouse.getStorageType());

            warehouse.setEditedAt(LocalDateTime.now());
            warehouse.setUser(optionalUser.get());

            warehouseRepository.save(warehouse);
            return "SUCCESS";
        } else {
            throw new RuntimeException("Warehouse not found with ID: " + id);
        }
    }

    public String deleteWarehouse(Long id, Long userId) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Warehouse not found with ID: " + id));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        warehouse.setStatus(StatusEnum.DELETE);
        warehouse.setUser(user);
        warehouseRepository.save(warehouse);
        return "Warehouse deleted successfully";
    }

    public Warehouse getWarehousById(Long id) {
        Optional<Warehouse> optionalWarehouse = warehouseRepository.findById(id);
        if (optionalWarehouse.isPresent()) {
            Warehouse warehouse = optionalWarehouse.get();
            return warehouse;
        } else {
            throw new RuntimeException("Entrepot non trouvé avec l'ID : " + id);
        }
    }
}
