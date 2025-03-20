package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.CreateUserDto;
import com.xpertpro.bbd_project.entity.RolesEntity;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.mapper.UserDtoMapper;
import com.xpertpro.bbd_project.repository.RoleRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    private UserDtoMapper userMapper;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public String encodePassword(String userPassword) {
        return passwordEncoder.encode(userPassword);
    }

    public String createUser(CreateUserDto userDto) {
        Optional<UserEntity> optionalUser = Optional.ofNullable(userRepository.findByUsername(userDto.getUsername()));
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            return "EMAIL_EXIST";
        }

        if (optionalUser.isPresent()) {
            return "USERNAME_EXIST";
        }

        if (userRepository.findByPhoneNumber(userDto.getPhoneNumber()).isPresent()) {
            return "PHONE_EXIST";
        }

        RolesEntity role = roleRepository.findByName(userDto.getRoleName())
                .orElseThrow(() ->  new RuntimeException("ROLE_NOT_FOUND"));

        UserEntity user = userMapper.toEntity(userDto);
        user.setRole(role);
        userRepository.save(user);
        return "SUCCESS";
    }

}
