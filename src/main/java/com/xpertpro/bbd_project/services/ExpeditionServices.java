package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.ExpeditionDto;
import com.xpertpro.bbd_project.dto.harbor.HarborDto;
import com.xpertpro.bbd_project.dtoMapper.ExpeditionDtoMapper;
import com.xpertpro.bbd_project.entity.Expeditions;
import com.xpertpro.bbd_project.entity.Harbor;
import com.xpertpro.bbd_project.entity.Partners;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.repository.ExpeditionRepository;
import com.xpertpro.bbd_project.repository.PartnerRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExpeditionServices {
    @Autowired
    ExpeditionRepository expeditionRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PartnerRepository clientRepo;
    @Autowired
    ExpeditionDtoMapper expeditionDtoMapper;

    public String create(ExpeditionDto dto, Long clientId, Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Partners client = clientRepo.findById(clientId).orElseThrow(() -> new RuntimeException("Client not found"));

        Expeditions expeditions = expeditionDtoMapper.toEntity(dto);

        expeditions.setCreatedAt(LocalDateTime.now());
        expeditions.setClient(client);
        expeditions.setCreatedBy(user);
        expeditionRepository.save(expeditions);
        return "SUCCESS";
    }
}
