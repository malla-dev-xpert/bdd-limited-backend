package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.devises.DeviseDto;
import com.xpertpro.bbd_project.entity.Devises;
import com.xpertpro.bbd_project.mapper.DeviseDtoMapper;
import com.xpertpro.bbd_project.repository.DevisesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class DeviseService {
    @Autowired
    DevisesRepository devisesRepository;
    @Autowired
    DeviseDtoMapper deviseDtoMapper;

    public String createDevise(DeviseDto deviseDto) {

        if (devisesRepository.findByCode(deviseDto.getCode()).isPresent()) {
            return "CODE_EXIST";
        }

        Devises devises = deviseDtoMapper.toEntity(deviseDto);

        devises.setCreatedAt(deviseDto.getCreatedAt());
        devisesRepository.save(devises);
        return "SUCCESS";
    }

    public DeviseDto updateDevise(Long id, DeviseDto deviseDto) {
        Optional<Devises> optionalDevises = devisesRepository.findById(id);

        if (optionalDevises.isPresent()) {
            Devises devises = optionalDevises.get();

            if (deviseDto.getCode() != null) devises.setCode(deviseDto.getCode());
            if (deviseDto.getName() != null) devises.setName(deviseDto.getName());
            if (deviseDto.getRate() != null) devises.setRate(deviseDto.getRate());
            devises.setEditedAt(deviseDto.getEditedAt());

            devisesRepository.save(devises);
            return deviseDto;
        } else {
            throw new RuntimeException("Devises not found with ID: " + id);
        }
    }
}
