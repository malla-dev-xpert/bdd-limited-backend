package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.user.CreateUserDto;
import com.xpertpro.bbd_project.dto.user.EditPasswordDto;
import com.xpertpro.bbd_project.dto.user.UpdateUserDto;
import com.xpertpro.bbd_project.dto.user.findUserDto;
import com.xpertpro.bbd_project.entity.RolesEntity;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.mapper.UserDtoMapper;
import com.xpertpro.bbd_project.repository.RoleRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Optional;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    SpringTemplateEngine templateEngine;
    @Autowired
    private UserDtoMapper userMapper;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JavaMailSender mailSender;

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

    private String generateRandomPassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%^&*()-_=+;?/|!~";
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * characters.length());
            password.append(characters.charAt(randomIndex));
        }
        return password.toString();
    }

    public String createUser(CreateUserDto userDto) {
        Optional<UserEntity> optionalUser = Optional.ofNullable(userRepository.findByUsername(userDto.getUsername()));
        String randomPassword = generateRandomPassword(10);
        // Stocker le mot de passe en clair temporairement
        String clearTextPassword = randomPassword;

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
        user.setPassword(encodePassword(randomPassword));
        userRepository.save(user);
        try {
            if (user.getEmail() != null && user.getStatusEnum() == StatusEnum.CREATE) {
                Context context = new Context();
                context.setVariable("firstName", user.getFirstName());
                context.setVariable("lastName", user.getLastName());
                context.setVariable("username", user.getUsername());
                context.setVariable("password", clearTextPassword);
                context.setVariable("roleName", user.getRole().getName());
                context.setVariable("createdAt", user.getCreatedAt());

                String htmlContent = templateEngine.process("register", context);

                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setTo(user.getEmail());
                helper.setSubject("Bienvenue sur BBD-LIMITED – Vos informations de connexion");
                helper.setText(htmlContent, true);

                mailSender.send(mimeMessage);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "SUCCESS";
    }

    public findUserDto getUserById(Long id) {
        Optional<UserEntity> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            return new findUserDto(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getPhoneNumber(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getStatusEnum().toString(),
                    user.getRole()
            );
        } else {
            throw new RuntimeException("Utilisateur non trouvé avec l'ID : " + id);
        }
    }

    public UpdateUserDto updateUser(Long userId, UpdateUserDto updateUserDto) {
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();

            if (updateUserDto.getFirstName() != null) user.setFirstName(updateUserDto.getFirstName());
            if (updateUserDto.getLastName() != null) user.setLastName(updateUserDto.getLastName());
            if (updateUserDto.getPhoneNumber() != null) user.setPhoneNumber(updateUserDto.getPhoneNumber());
            if (updateUserDto.getEmail() != null) user.setEmail(updateUserDto.getEmail());
            if (updateUserDto.getUsername() != null) user.setUsername(updateUserDto.getUsername());
            user.setEditedAt(updateUserDto.getEditedAt());

            userRepository.save(user);
            return updateUserDto;
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    public String editPassword(Long userId, EditPasswordDto editPasswordDto) {
        Optional<UserEntity> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();

            // Vérifier si l'ancien mot de passe est correct
            if (!passwordEncoder.matches(editPasswordDto.getOldPassword(), user.getPassword())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OLD_PASSWORD_INCORRECT");
            }

            // Hacher le nouveau mot de passe et mettre à jour l'utilisateur
            String hashedNewPassword = passwordEncoder.encode(editPasswordDto.getNewPassword());
            user.setPassword(hashedNewPassword);
            user.setEditedAt(editPasswordDto.getEditedAt());
            userRepository.save(user);

            return "PASSWORD_EDITED";
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }
}
