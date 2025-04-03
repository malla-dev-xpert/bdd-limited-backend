package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.harbor.HarborDto;
import com.xpertpro.bbd_project.entity.Harbor;
import com.xpertpro.bbd_project.entity.Partners;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.dtoMapper.HarborDtoMapper;
import com.xpertpro.bbd_project.repository.HarborRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public String updateHarbor(Long id, HarborDto newHarbor, Long userId) {
        Optional<Harbor> optionalHarbor = harborRepository.findById(id);
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (harborRepository.findByName(newHarbor.getName()).isPresent()) {
            return "NAME_EXIST";
        }

        if (optionalHarbor.isPresent() && optionalUser.isPresent()) {
            Harbor harbor = optionalHarbor.get();

            if (newHarbor.getLocation() != null) harbor.setLocation(newHarbor.getLocation());
            if (newHarbor.getName() != null) harbor.setName(newHarbor.getName());

            harbor.setEditedAt(newHarbor.getEditedAt());
            harbor.setUser(optionalUser.get());

            harborRepository.save(harbor);
            return "SUCCESS";
        } else {
            throw new RuntimeException("Warehouse not found with ID: " + id);
        }
    }

    public String deleteHarbor(Long id, Long userId) {
        Harbor harbor = harborRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Harbor not found with ID: " + id));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        harbor.setStatus(StatusEnum.DELETE);
        harbor.setUser(user);
        harborRepository.save(harbor);
        return "Harbor deleted successfully";
    }

    public String disableHarbor(Long id, Long userId) {
        Harbor harbor = harborRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Harbor not found with ID: " + id));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        harbor.setStatus(StatusEnum.DISABLE);
        harbor.setUser(user);
        harborRepository.save(harbor);
        return "Harbor disable successfully";
    }

    public Page<Harbor> findAllHarbor(int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").ascending());
        return harborRepository.findByStatus(StatusEnum.CREATE, pageable);
    }

    public Harbor getHarborById(Long id) {
        Optional<Harbor> optionalHarbor = harborRepository.findById(id);
        if (optionalHarbor.isPresent()) {
            Harbor harbor = optionalHarbor.get();
            return harbor;
        } else {
            throw new RuntimeException("Port non trouvé avec l'ID : " + id);
        }
    }
}
