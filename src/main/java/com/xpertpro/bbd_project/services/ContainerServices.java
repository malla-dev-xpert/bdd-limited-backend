package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.containers.ContainersDto;
import com.xpertpro.bbd_project.entity.Carriers;
import com.xpertpro.bbd_project.entity.Containers;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.ContainersRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public String updateContainer(Long id, ContainersDto containersDto, Long userId) {
        Containers newContainer = containersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conteneur non trouvé"));
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (containersRepository.findByReference(containersDto.getReference()).isPresent()) {
            return "REF_EXIST";
        }

        if(optionalUser.isPresent()){
            if (containersDto.getReference() != null) newContainer.setReference(containersDto.getReference());
            if (containersDto.getIsAvailable() != null) newContainer.setIsAvailable(containersDto.getIsAvailable());
            newContainer.setEditedAt(containersDto.getEditedAt());

            containersRepository.save(newContainer);
            return "SUCCESS";
        }else{
            throw new RuntimeException("User not found with ID: " + id);
        }
    }

    public Page<Containers> getAllContainers(int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").ascending());
        return containersRepository.findByStatus(StatusEnum.CREATE, pageable);
    }

    public Containers getContainerById(Long id) {
        Optional<Containers> optionalContainers = containersRepository.findById(id);
        if (optionalContainers.isPresent()) {
            Containers containers = optionalContainers.get();
            return containers;
        } else {
            throw new RuntimeException("Conteneur non trouvé avec l'ID : " + id);
        }
    }

    public String deleteContainer(Long id, Long userId) {
        Containers containers = containersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Container not found with ID: " + id));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        containers.setStatus(StatusEnum.DELETE);
        containers.setUser(user);
        containersRepository.save(containers);
        return "Container deleted successfully";
    }
}
