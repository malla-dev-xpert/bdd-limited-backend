package com.xpertpro.bbd_project.controllers;

import com.xpertpro.bbd_project.config.JwtUtil;
import com.xpertpro.bbd_project.dto.user.CreateUserDto;
import com.xpertpro.bbd_project.dto.user.EditPasswordDto;
import com.xpertpro.bbd_project.dto.user.UpdateUserDto;
import com.xpertpro.bbd_project.dto.user.findUserDto;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.PermissionsEnum;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.logs.SessionLog;
import com.xpertpro.bbd_project.repository.SessionLogRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import com.xpertpro.bbd_project.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/users")
@CrossOrigin("*")
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    HttpServletRequest request;

    @Autowired
    SessionLogRepository sessionLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    SpringTemplateEngine templateEngine;

    @PostMapping("/auth")
    public ResponseEntity<String> login(@RequestBody UserEntity user) {
        UserEntity existingUser = userRepository.findByUsername(user.getUsername());

        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé.");
        }

        if (existingUser.getStatusEnum() == StatusEnum.DISABLE) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Votre compte est suspendu pour le moment.");
        }

        if (existingUser.getStatusEnum() == StatusEnum.CREATE) {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );

            if (authentication.isAuthenticated()) {
                String token = jwtUtil.generateToken(user.getUsername());
                String ipAddress = request.getRemoteAddr(); // Récupérer l'adresse IP

                // Enregistrer la session active
                SessionLog session = new SessionLog();
                session.setUsername(user.getUsername());
                session.setIpAddress(ipAddress);
                session.setLoginTime(LocalDateTime.now());
                session.setSuccessful(authentication.isAuthenticated());
                session.setJwtToken(token);
                sessionLogRepository.save(session);

                return ResponseEntity.ok(token);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiants incorrects");
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Statut utilisateur non valide.");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        // Récupérer le token depuis le header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token manquant");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        // Valider le token
        if (username == null || !jwtUtil.validateToken(token, username)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide");
        }

        // Trouver la session active
        SessionLog activeSession = sessionLogRepository
                .findByUsernameAndJwtTokenAndLogoutTimeIsNull(username, token);

        if (activeSession == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session introuvable");
        }

        // Mettre à jour le log de session
        activeSession.setLogoutTime(LocalDateTime.now());
        sessionLogRepository.save(activeSession);

        return ResponseEntity.ok("Déconnexion réussie");
    }

    @PostMapping("/{username}/force-user-logout")
    public ResponseEntity<String> forceLogout(@PathVariable String username) {
        return userService.forceLogout(username);
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

    @GetMapping("/id/{id}")
    public findUserDto getUserById(@PathVariable("id") Long id){
        return userService.getUserById(id);
    }

    @PutMapping("/update/{id}")
    public UpdateUserDto updateUser(@PathVariable Long id, @RequestBody UpdateUserDto updateUserDto) {
        return userService.updateUser(id, updateUserDto);
    }

    @PutMapping("/change-password/{id}")
    public String changeUserPassword(@PathVariable Long id, @RequestBody EditPasswordDto editPasswordDto) {
        return userService.editPassword(id, editPasswordDto);
    }

    @DeleteMapping("/disable/{id}")
    public String disableUser(@PathVariable Long id){
        userService.disableUser(id);
        return "Le compte de l'utilisateur a été désactivé.";
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @RequestParam("deleteUser") Long userId,
            @RequestParam("admin") UserEntity currentUser) {

        String result = userService.deleteUser(userId, currentUser);

        return ResponseEntity.ok()
                .body(Map.of(
                        "status", "success",
                        "message", result,
                        "deletedUserId", userId,
                        "timestamp", LocalDateTime.now()
                ));
    }


    @GetMapping()
    public List<CreateUserDto> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(required = false) String query) {
        return userService.getAllUsers(page, query);
    }

}
