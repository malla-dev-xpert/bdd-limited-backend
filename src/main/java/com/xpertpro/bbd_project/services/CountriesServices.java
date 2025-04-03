package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.CountriesDto;
import com.xpertpro.bbd_project.entity.Countries;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.CountriesRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CountriesServices {
    @Autowired
    CountriesRepository countriesRepository;
    @Autowired
    UserRepository userRepository;

    public String createCountries(CountriesDto countriesDto, Long userId) {
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (countriesRepository.findByName(countriesDto.getName()).isPresent()) {
            return "NAME_EXIST";
        }

        if (countriesRepository.findByIsoCode(countriesDto.getIsoCode()).isPresent()) {
            return "ISO_EXIST";
        }

        if(optionalUser.isPresent()){
            Countries countries = new Countries();

            countries.setName(countriesDto.getName());
            countries.setIsoCode(countriesDto.getIsoCode());
            countries.setCreatedAt(countriesDto.getCreatedAt());
            countries.setUser(optionalUser.get());

            countriesRepository.save(countries);
            return "SUCCESS";
        }else{
            throw new RuntimeException("User not found with ID: " + userId);
        }

    }

    public String updateCountries(Long id, CountriesDto countriesDto, Long userId) {
        Countries newCountries = countriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pays non trouvé"));
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (countriesRepository.findByName(countriesDto.getName()).isPresent()) {
            return "NAME_EXIST";
        }

        if (countriesRepository.findByIsoCode(countriesDto.getIsoCode()).isPresent()) {
            return "ISO_EXIST";
        }

        if(optionalUser.isPresent()){
            if (countriesDto.getIsoCode() != null) newCountries.setIsoCode(countriesDto.getIsoCode());
            if (countriesDto.getName() != null) newCountries.setName(countriesDto.getName());
            newCountries.setEditedAt(countriesDto.getEditedAt());

            countriesRepository.save(newCountries);
            return "SUCCESS";
        }else{
            throw new RuntimeException("User not found with ID: " + id);
        }
    }

    public Page<Countries> getAllCountries(int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").ascending());
        return countriesRepository.findByStatus(StatusEnum.CREATE, pageable);
    }

    public Countries getCountryById(Long id) {
        Optional<Countries> optionalCountries = countriesRepository.findById(id);
        if (optionalCountries.isPresent()) {
            Countries countries = optionalCountries.get();
            return countries;
        } else {
            throw new RuntimeException("Pays non trouvé avec l'ID : " + id);
        }
    }

    public String deleteCountry(Long id, Long userId) {
        Countries countries = countriesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pays not found with ID: " + id));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        countries.setStatus(StatusEnum.DELETE);
        countries.setUser(user);
        countriesRepository.save(countries);
        return "Country deleted successfully";
    }
}
