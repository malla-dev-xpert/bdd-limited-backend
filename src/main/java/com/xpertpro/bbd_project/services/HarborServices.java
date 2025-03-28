package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.devises.DeviseDto;
import com.xpertpro.bbd_project.dto.harbor.HarborDto;
import com.xpertpro.bbd_project.entity.Devises;
import com.xpertpro.bbd_project.entity.Harbor;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.mapper.HarborDtoMapper;
import com.xpertpro.bbd_project.repository.HarborRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HarborServices {
    @Autowired
    HarborRepository harborRepository;
    @Autowired
    HarborDtoMapper harborDtoMapper;
    @Autowired
    UserRepository userRepository;

    public String createHarbor(HarborDto harborDto, Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (harborRepository.findByName(harborDto.getName()).isPresent()) {
            return "NAME_EXIST";
        }

        Harbor harbor = harborDtoMapper.toEntity(harborDto);

        harbor.setCreatedAt(harborDto.getCreatedAt());
        harbor.setUser(user);
        harborRepository.save(harbor);
        return "SUCCESS";
    }
}
