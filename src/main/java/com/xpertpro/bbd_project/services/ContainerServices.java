package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.containers.ContainersDto;
import com.xpertpro.bbd_project.entity.Containers;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.repository.ContainersRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ContainerServices {
    @Autowired
    ContainersRepository containersRepository;
    @Autowired
    UserRepository userRepository;


    public String createContainer(ContainersDto containersDto, Long userId) {
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (containersRepository.findByReference(containersDto.getReference()).isPresent()) {
            return "REF_EXIST";
        }

        if(optionalUser.isPresent()){
            Containers containers = new Containers();

            containers.setReference(containersDto.getReference());
            containers.setIsAvailable(containersDto.getIsAvailable());
            containers.setCreatedAt(containersDto.getCreatedAt());
            containers.setUser(optionalUser.get());

            containersRepository.save(containers);
            return "SUCCESS";
        }else{
            throw new RuntimeException("User not found with ID: " + userId);
        }

    }
}
