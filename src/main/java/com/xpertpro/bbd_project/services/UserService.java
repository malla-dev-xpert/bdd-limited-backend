package com.xpertpro.bbd_project.services;

import com.xpertpro.bbd_project.config.JwtUtil;
import com.xpertpro.bbd_project.dto.user.CreateUserDto;
import com.xpertpro.bbd_project.dto.user.EditPasswordDto;
import com.xpertpro.bbd_project.dto.user.UpdateUserDto;
import com.xpertpro.bbd_project.dto.user.findUserDto;
import com.xpertpro.bbd_project.entity.RolesEntity;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.PermissionsEnum;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.logs.SessionLog;
import com.xpertpro.bbd_project.dtoMapper.UserDtoMapper;
import com.xpertpro.bbd_project.repository.RoleRepository;
import com.xpertpro.bbd_project.repository.SessionLogRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Autowired
    SpringTemplateEngine templateEngine;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDtoMapper userMapper;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    SessionLogRepository sessionLogRepository;
    @Autowired
    private LogServices logServices;

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

        userRepository.save(user);
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
        // Vérifier que les données requises sont présentes
        if (userId == null) {
            throw new RuntimeException("L'ID de l'utilisateur est requis");
        }
        if (updateUserDto == null) {
            throw new RuntimeException("Les données de mise à jour sont requises");
        }

        // Récupérer l'utilisateur existant
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + userId));

        // Mettre à jour les champs un par un
        if (updateUserDto.getFirstName() != null && !updateUserDto.getFirstName().isEmpty()) {
            user.setFirstName(updateUserDto.getFirstName());
        }

        if (updateUserDto.getLastName() != null && !updateUserDto.getLastName().isEmpty()) {
            user.setLastName(updateUserDto.getLastName());
        }

        if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().isEmpty()) {
            if (!updateUserDto.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(updateUserDto.getEmail())) {
                    throw new RuntimeException("Cet email est déjà utilisé par un autre utilisateur");
                }
                user.setEmail(updateUserDto.getEmail());
            }
        }

        if (updateUserDto.getPhoneNumber() != null && !updateUserDto.getPhoneNumber().isEmpty()) {
            user.setPhoneNumber(updateUserDto.getPhoneNumber());
        }

        if (updateUserDto.getUsername() != null && !updateUserDto.getUsername().isEmpty()) {
            user.setUsername(updateUserDto.getUsername());
        }

        // Sauvegarder les modifications
        userRepository.save(user);

        // Retourner les données mises à jour
        return updateUserDto;
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

    public String disableUser(Long userId) {
        Optional<UserEntity> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            UserEntity user = optionalUser.get();
            user.setStatusEnum(StatusEnum.DISABLE);
            userRepository.save(user);

            if(user.getStatusEnum() == StatusEnum.DISABLE){
                // déconnecter un utilisateur après la désactivation de son compte
                SecurityContextHolder.getContext().setAuthentication(null);
                SecurityContextHolder.clearContext();

                try {
                    System.out.println("Statut de l'utilisateur est DISABLE, envoi de l'email...");
                    Context context = new Context();
                    context.setVariable("firstName", user.getFirstName());
                    context.setVariable("lastName", user.getLastName());
                    context.setVariable("username", user.getUsername());
                    context.setVariable("editedAt", user.getEditedAt());

                    String htmlContent = templateEngine.process("disable-user", context);

                    MimeMessage mimeMessage = mailSender.createMimeMessage();
                    MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                    helper.setTo(user.getEmail());
                    helper.setSubject("Compte désactivé – BBD LIMITED");
                    helper.setText(htmlContent, true);

                    mailSender.send(mimeMessage);
                    return "user disable.";
                } catch (Exception e) {
                    throw new RuntimeException("Erreur lors de l'envoi de l'email : " + e.getMessage(), e);
                }
            }
        }
        throw new RuntimeException("User not found with ID: " + userId);
    }

    @Transactional
    public String deleteUser(Long userIdToDelete, UserEntity currentUser) {
        // 1. Validation de base
        validateUsers(userIdToDelete, currentUser);

        // 2. Récupération et vérification
        UserEntity userToDelete = userRepository.findById(userIdToDelete)
                .orElseThrow(() -> new EntityNotFoundException("Compte utilisateur introuvable"));

        checkPermissions(currentUser, userToDelete);

        // 3. Mise à jour du statut
        markUserAsDeleted(userToDelete, currentUser);

        // 4. Gestion de session
        handleUserSession(currentUser, userToDelete);

        // 5. Notification par email
        sendDeletionEmail(userToDelete, currentUser);

        return String.format("Le compte %s a été supprimé avec succès. Une notification a été envoyée à %s",
                userToDelete.getUsername(),
                userToDelete.getEmail());
    }

    private void validateUsers(Long userIdToDelete, UserEntity currentUser) {
        if (userIdToDelete == null || currentUser == null) {
            throw new IllegalArgumentException("Données utilisateur invalides");
        }
        if (userIdToDelete.equals(currentUser.getId())) {
            System.out.println("Tentative d'auto-suppression par {}"+ currentUser.getUsername());
        }
    }

    private void checkPermissions(UserEntity currentUser, UserEntity userToDelete) {
        if (!currentUser.getRole().getPermissions().contains(PermissionsEnum.IS_ADMIN)) {
            throw new SecurityException("Privilèges insuffisants pour effectuer cette action");
        }
    }

    private void markUserAsDeleted(UserEntity userToDelete, UserEntity deletedBy) {
        userToDelete.setStatusEnum(StatusEnum.DELETE);
        userToDelete.setEditedAt(LocalDateTime.now());
        logServices.logAction(
                deletedBy,
                "DELETE_USER",
                "Users",
                userToDelete.getId());
        userRepository.save(userToDelete);
    }

    private void handleUserSession(UserEntity currentUser, UserEntity userToDelete) {
        if (currentUser.getId().equals(userToDelete.getId())) {
            SecurityContextHolder.clearContext();
            System.out.println("Utilisateur {} s'est auto-désactivé"+ userToDelete.getUsername());
        }
    }

    private void sendDeletionEmail(UserEntity deletedUser, UserEntity deletedBy) {
        try {
            MimeMessage message = buildDeletionEmail(deletedUser, deletedBy);
            mailSender.send(message);
            System.out.println("Email de notification envoyé à {}"+ deletedUser.getEmail());
        } catch (Exception e) {
            System.out.println("Échec d'envoi de l'email à {}"+ deletedUser.getEmail()+ e);
        }
    }

    private MimeMessage buildDeletionEmail(UserEntity deletedUser, UserEntity deletedBy) throws MessagingException {
        Context context = new Context();
        context.setVariable("firstName", deletedUser.getFirstName());
        context.setVariable("lastName", deletedUser.getLastName());
        context.setVariable("username", deletedUser.getUsername());
        context.setVariable("editedAt", LocalDateTime.now());

        String htmlContent = templateEngine.process("delete-user", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(deletedUser.getEmail()); // Email envoyé uniquement à l'utilisateur supprimé
        helper.setSubject("Compte Supprimé – BBD LIMITED");
        helper.setText(htmlContent, true);

        return message;
    }

    public ResponseEntity<String> forceLogout(String username) {
        // Vérifier l'existence de l'utilisateur
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Utilisateur non trouvé");
        }

        // Trouver toutes les sessions actives de l'utilisateur
        List<SessionLog> activeSessions = sessionLogRepository
                .findByUsernameAndLogoutTimeIsNull(username);

        if (activeSessions.isEmpty()) {
            return ResponseEntity.ok("Aucune session active pour " + username);
        }

        // Invalider chaque session individuellement
        activeSessions.forEach(session -> {
            session.setLogoutTime(LocalDateTime.now());
            sessionLogRepository.save(session);
        });

        return ResponseEntity.ok(
                String.format("Déconnexion forcée réussie - %d sessions invalidées",
                        activeSessions.size())
        );

    }

    public List<CreateUserDto> getAllUsers(int page, String query) {
        int pageSize = 30;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());

        Page<UserEntity> users = userRepository.findByStatusEnum(StatusEnum.CREATE, pageable);

        if (query != null && !query.isEmpty()) {
            users = userRepository.findByStatusEnumAndSearchQuery(
                    StatusEnum.CREATE,
                    "%" + query.toLowerCase() + "%",
                    pageable
            );
        } else {
            users = userRepository.findByStatusEnum(StatusEnum.CREATE, pageable);
        }


        return users.stream()
                .filter(user -> user.getStatusEnum() != StatusEnum.DELETE)
                .sorted(Comparator.comparing(UserEntity::getCreatedAt).reversed())
                .map(user -> {
                    CreateUserDto dto = new CreateUserDto();
                    dto.setId(user.getId());
                    dto.setUsername(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());
                    dto.setPhoneNumber(user.getPhoneNumber());
                    dto.setRoleName(user.getRole() != null ? user.getRole().getName() : null);

                    return dto;
                })
                .collect(Collectors.toList());

    }

}
