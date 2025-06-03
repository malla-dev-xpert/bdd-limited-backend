package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.devises.DeviseDto;
import com.xpertpro.bbd_project.entity.Devises;
import com.xpertpro.bbd_project.entity.ExchangeRate;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.dtoMapper.DeviseDtoMapper;
import com.xpertpro.bbd_project.repository.DevisesRepository;
import com.xpertpro.bbd_project.repository.ExchangeRateRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;


@Service
public class DeviseService {
    @Autowired
    DevisesRepository devisesRepository;
    @Autowired
    DeviseDtoMapper deviseDtoMapper;
    @Autowired
    ExchangeRateRepository exchangeRateRepository;
    @Autowired
    LogServices logServices;
    @Autowired
    UserRepository userRepository;

    private final RestTemplate restTemplate;
    public DeviseService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String createDevise(DeviseDto deviseDto, Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (devisesRepository.findByName(deviseDto.getName()).isPresent()) {
            return "NAME_EXIST";
        }
        if (devisesRepository.findByCode(deviseDto.getCode()).isPresent()) {
            return "CODE_EXIST";
        }

        Devises devises = deviseDtoMapper.toEntity(deviseDto);

        devises.setCreatedAt(deviseDto.getCreatedAt());
        devises.setUser(user);
        Devises newDevise = devisesRepository.save(devises);

        logServices.logAction(user,"CREATE_DEVISE","Devises", newDevise.getId());

        return "SUCCESS";
    }

    public DeviseDto updateDevise(Long id, DeviseDto deviseDto) {
        Optional<Devises> optionalDevises = devisesRepository.findById(id);

        if (optionalDevises.isPresent()) {
            Devises devises = optionalDevises.get();

            if (deviseDto.getCode() != null) devises.setCode(deviseDto.getCode());
            if (deviseDto.getName() != null) devises.setName(deviseDto.getName());
//            if (deviseDto.getRate() != null) devises.setRate(deviseDto.getRate());
            devises.setEditedAt(deviseDto.getEditedAt());

            devisesRepository.save(devises);
            return deviseDto;
        } else {
            throw new RuntimeException("Devises not found with ID: " + id);
        }
    }

    public Page<Devises> getAllDevises(int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());
        return devisesRepository.findByStatus(StatusEnum.CREATE, pageable);
    }

    public String deleteDevises(Long id) {
        Devises devises = devisesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devise not found with ID: " + id));

        devises.setStatus(StatusEnum.DELETE);
        devisesRepository.save(devises);
        return "Devises deleted successfully";
    }

    public Devises getDeviseById(Long id) {
        Optional<Devises> optionalDevises = devisesRepository.findById(id);
        if (optionalDevises.isPresent()) {
            Devises devises = optionalDevises.get();
            return devises;
        } else {
            throw new RuntimeException("Devise non trouvé avec l'ID : " + id);
        }
    }

    public Double getRealTimeRate(String fromCode, String toCode) {
        String url = String.format("https://api.exchangerate.host/convert?from=%s&to=%s", fromCode, toCode);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map body = response.getBody();
            return (Double) body.get("result");
        }
        throw new RuntimeException("Échec de récupération du taux de change.");
    }

    public ExchangeRate saveExchangeRate(String fromCode, String toCode) {
        Double rate = getRealTimeRate(fromCode, toCode);
        Devises from = devisesRepository.findByCode(fromCode).orElseThrow();
        Devises to = devisesRepository.findByCode(toCode).orElseThrow();

        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setFromDevise(from);
        exchangeRate.setToDevise(to);
        exchangeRate.setRate(rate);
        exchangeRate.setTimestamp(LocalDateTime.now());
        return exchangeRateRepository.save(exchangeRate);
    }

}
