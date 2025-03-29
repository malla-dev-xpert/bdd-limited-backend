package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.carrier.CarrierDto;
import com.xpertpro.bbd_project.entity.CarrierServiceEntity;
import com.xpertpro.bbd_project.entity.Carriers;
import com.xpertpro.bbd_project.entity.Devises;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.CarrierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarrierServices {

    @Autowired
    CarrierRepository carrierRepository;

    public String createCarrier(CarrierDto carrierDto) {
        if (carrierRepository.findByContact(carrierDto.getContact()).isPresent()) {
            return "CONTACT_EXIST";
        }

        Carriers carriers = new Carriers();

        carriers.setName(carrierDto.getName());
        carriers.setContact(carrierDto.getContact());
        carriers.setCreatedAt(carrierDto.getCreatedAt());

        List<CarrierServiceEntity> services = carrierDto.getServices().stream()
                .map(name -> {
                    CarrierServiceEntity service = new CarrierServiceEntity();
                    service.setName(name);
                    service.setCarriers(carriers);
                    service.setCreatedAt(carriers.getCreatedAt());
                    return service;
                })
                .collect(Collectors.toList());

        carriers.setCarrierService(services);
        carrierRepository.save(carriers);
        return "SUCCESS";
    }

    public Page<Carriers> getAllCarriers(int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").ascending());
        return carrierRepository.findByStatus(StatusEnum.CREATE, pageable);
    }
}
