package com.xpertpro.bbd_project.dtoMapper;

import com.xpertpro.bbd_project.dto.PackageDto;
import com.xpertpro.bbd_project.entity.Harbor;
import com.xpertpro.bbd_project.entity.Packages;
import com.xpertpro.bbd_project.repository.HarborRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PackageDtoMapper {
    @Autowired
    HarborRepository harborRepository;
    public Packages toEntity(PackageDto dto) {
        Packages packages = new Packages();
        packages.setExpeditionType(dto.getExpeditionType());
        packages.setArrivalDate(dto.getArrivalDate());
        packages.setCbn(dto.getCbn());
        Harbor startPort = harborRepository.findById(dto.getStartHarborId())
                .orElseThrow(() -> new EntityNotFoundException("Start port not found"));
        Harbor destinationPort = harborRepository.findById(dto.getDestinationHarborId())
                .orElseThrow(() -> new EntityNotFoundException("Destination port not found"));

        packages.setStartCountry(startPort.getName());
        packages.setDestinationCountry(destinationPort.getName());
        packages.setItemQuantity(dto.getItemQuantity());
        packages.setStartDate(dto.getStartDate());
        packages.setWeight(dto.getWeight());
        packages.setRef(dto.getRef());
        return packages;
    }
}
