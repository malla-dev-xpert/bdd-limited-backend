package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.carrier.CarrierDto;
import com.xpertpro.bbd_project.entity.*;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.CarrierRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarrierServices {

    @Autowired
    CarrierRepository carrierRepository;
    @Autowired
    UserRepository userRepository;

    public String createCarrier(CarrierDto carrierDto, Long userId) {
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (carrierRepository.findByContact(carrierDto.getContact()).isPresent()) {
            return "CONTACT_EXIST";
        }

        if(optionalUser.isPresent()){
            Carriers carriers = new Carriers();

            carriers.setName(carrierDto.getName());
            carriers.setContact(carrierDto.getContact());
            carriers.setCreatedAt(carrierDto.getCreatedAt());
            carriers.setUser(optionalUser.get());

            List<CarrierServiceEntity> services = carrierDto.getServices().stream()
                    .map(name -> {
                        CarrierServiceEntity service = new CarrierServiceEntity();
                        service.setName(name);
                        service.setCarriers(carriers);
                        service.setCreatedAt(carriers.getCreatedAt());
                        service.setUser(optionalUser.get());
                        return service;
                    })
                    .collect(Collectors.toList());

            carriers.setCarrierService(services);
            carrierRepository.save(carriers);
            return "SUCCESS";
        }else{
            throw new RuntimeException("User not found with ID: " + userId);
        }

    }

    public Page<Carriers> getAllCarriers(int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").ascending());
        return carrierRepository.findByStatus(StatusEnum.CREATE, pageable);
    }

    public String updateCarrier(Long id, CarrierDto carrierDto, Long userId) {
        Carriers newCarrier = carrierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transporteur non trouvé"));
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (carrierRepository.findByContact(carrierDto.getName()).isPresent()) {
            return "CONTACT_EXIST";
        }

        if(optionalUser.isPresent()){
            if (carrierDto.getName() != null) newCarrier.setName(carrierDto.getName());
            if (carrierDto.getContact() != null) newCarrier.setContact(carrierDto.getName());
            newCarrier.setEditedAt(carrierDto.getEditedAt());

            carrierRepository.save(newCarrier);
            return "SUCCESS";
        }else{
            throw new RuntimeException("User not found with ID: " + id);
        }
    }

    public String deleteCarrier(Long id, Long userId) {
        Carriers carriers = carrierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Carrier not found with ID: " + id));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        carriers.setStatus(StatusEnum.DELETE);
        carriers.setUser(user);
        carrierRepository.save(carriers);
        return "Carrier deleted successfully";
    }
}
