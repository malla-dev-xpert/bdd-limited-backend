package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.dto.CreateUserDto;
import com.xpertpro.bbd_project.entity.RolesEntity;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.mapper.UserDtoMapper;
import com.xpertpro.bbd_project.repository.RoleRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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
//
//        try{
//            if(user.getEmail() != null){
//                if(user.getStatusEnum() == StatusEnum.CREATE){
//                    SimpleMailMessage message = new SimpleMailMessage();
//                    message.setTo(user.getEmail());
//                    message.setSubject("Bienvenue sur BBD-LIMITED – Vos informations de connexion");
//                    message.setText
//                            ("Bonjour "+ user.getFirstName() + " " + user.getLastName() + "\n" +
//                                    "Nous sommes ravis de vous accueillir sur BBD-LIMITED ! Votre compte a été créé avec succès. Vous trouverez ci-dessous vos informations de connexion :"+
//                                    "\n"+
//                                    "\n"+
//                                    "Username : "+user.getUsername()+"\n"+
//                                    "Mot de passe : " +clearTextPassword+"\n"+
//                                    "Fait le : "+ user.getCreatedAt()+
//                                    "\n"+
//                                    "\n"+
//                                    "\n"+
//                                    "Nous vous recommandons de modifier votre mot de passe dès votre première connexion pour des raisons de sécurité."
//                                    + "\n" + "\n"+
//                                    "Si vous avez des questions ou besoin d’assistance, n’hésitez pas à nous contacter à bbdlimited@gmail.com."
//                                    + "\n"+"\n"+"\n"+
//                                    "Bienvenue et bonne expérience sur BBD LIMITED !"
//                            );
//                    mailSender.send(message);
//                }
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        return "SUCCESS";
    }

}
