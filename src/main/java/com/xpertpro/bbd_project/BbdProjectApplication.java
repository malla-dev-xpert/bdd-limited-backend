package com.xpertpro.bbd_project;

import com.xpertpro.bbd_project.entity.RolesEntity;
import com.xpertpro.bbd_project.entity.UserEntity;
import com.xpertpro.bbd_project.enums.PermissionsEnum;
import com.xpertpro.bbd_project.enums.StatusEnum;
import com.xpertpro.bbd_project.repository.RoleRepository;
import com.xpertpro.bbd_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

@SpringBootApplication
public class BbdProjectApplication {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository rolesRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(BbdProjectApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public CommandLineRunner initializeDefaultAdmin() {
		return args -> {
			// Vérifier si le rôle admin existe déjà
			RolesEntity adminRole = rolesRepository.findByName("ADMINISTRATREUR").orElse(null);

			if (adminRole == null) {
				// Créer le rôle admin avec toutes les permissions
				adminRole = new RolesEntity();
				adminRole.setName("ADMINISTRATREUR");
				adminRole.setPermissions(Set.of(
						PermissionsEnum.CAN_READ,
						PermissionsEnum.CAN_WRITE,
						PermissionsEnum.CAN_UPDATE,
						PermissionsEnum.CAN_DELETE,
						PermissionsEnum.IS_ADMIN
				));
				rolesRepository.save(adminRole);
			}

			// Vérifier si l'utilisateur admin existe déjà
			if (!userRepository.existsByEmail("admin@bbdproject.com")) {
				UserEntity adminUser = new UserEntity();
				adminUser.setFirstName("System");
				adminUser.setLastName("Administrateur");
				adminUser.setEmail("admin@bbdproject.com");
				adminUser.setPhoneNumber("+1234567890");
				adminUser.setUsername("admin");
				adminUser.setPassword(passwordEncoder.encode("admin123")); // Mot de passe par défaut
				adminUser.setStatusEnum(StatusEnum.CREATE);
				adminUser.setRole(adminRole);

				userRepository.save(adminUser);

				System.out.println("==============================================");
				System.out.println("ADMINISTRATEUR PAR DÉFAUT CRÉÉ AVEC SUCCÈS");
				System.out.println("Email: admin@bbdproject.com");
				System.out.println("Mot de passe: admin123");
				System.out.println("⚠️ CHANGEZ LE MOT DE PASSE APRÈS LA PREMIÈRE CONNEXION!");
				System.out.println("==============================================");
			} else {
				System.out.println("Administrateur par défaut existe déjà");
			}
		};
	}
}