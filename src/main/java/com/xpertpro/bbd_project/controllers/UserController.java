package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.config.JwtUtil;
import com.xpertpro.bbd_project.dto.CreateUserDto;
import com.xpertpro.bbd_project.dto.findUserDto;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.repository.UserRepository;
import com.xpertpro.bbd_project.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/users")
@CrossOrigin("*")
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/auth")
    public ResponseEntity<String> login(@RequestBody UserEntity user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );
        if (authentication.isAuthenticated()) {
            String token = jwtUtil.generateToken(user.getUsername());
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(401).body("ERROR");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> register(@RequestBody CreateUserDto userDto) {
        String result = userService.createUser(userDto);
        switch (result) {
            case "EMAIL_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email déjà utilisé !");
            case "USERNAME_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Nom d'utilisateur déjà pris !");
            case "PHONE_EXIST":
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Numéro de téléphone déjà enregistré !");
            default:
                return ResponseEntity.status(HttpStatus.CREATED).body("Utilisateur créé avec succès !");
        }
    }

    @GetMapping("/{username}")
    public UserEntity findByUsername(@PathVariable String username) {
        Optional<UserEntity> userEntity = Optional.ofNullable(userRepository.findByUsername(username));

        if(userEntity.isPresent()){
            return userRepository.findByUsername(username);
        }else{
            throw new IllegalArgumentException("Username not found");
        }
    }

}
