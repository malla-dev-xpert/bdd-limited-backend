package com.xpertpro.bbd_project.mapper;

import com.xpertpro.bbd_project.dto.user.CreateUserDto;
import com.xpertpro.bbd_project.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {

    public UserEntity toEntity(CreateUserDto dto) {
        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        return user;
    }
}
