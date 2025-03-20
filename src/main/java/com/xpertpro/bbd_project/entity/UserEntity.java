package com.xpertpro.bbd_project.entity;

import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    private String password;
    private String username;

    @Enumerated(EnumType.STRING)
    private StatusEnum statusEnum = StatusEnum.CREATE;

//    @ManyToOne
//    @JoinColumn(name = "role_id", nullable = false)
//    private RolesEntity userRole;
    private String roleName;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt;

}
