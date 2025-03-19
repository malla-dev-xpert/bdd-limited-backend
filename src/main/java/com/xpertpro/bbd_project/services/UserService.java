package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.entity.User;
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

    public String createUser(User user){
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByUsername(user.getUsername()));
        try{
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return "EMAIL_EXIST";
            }

            if (optionalUser.isPresent()) {
                return "USERNAME_EXIST";
            }

            if (userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
                return "PHONE_EXIST";
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));

            userRepository.save(user);
            return "SUCCESS";

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
