package com.xpertpro.bbd_project.dtoMapper;

import com.xpertpro.bbd_project.dto.CountriesDto;
import com.xpertpro.bbd_project.entity.Countries;
import org.springframework.stereotype.Component;

@Component
public class CountriesDtoMapper {
    public Countries toEntity(CountriesDto dto) {
        Countries countries = new Countries();
        countries.setName(dto.getName());
        countries.setIsoCode(dto.getIsoCode());
        return countries;
    }
}
