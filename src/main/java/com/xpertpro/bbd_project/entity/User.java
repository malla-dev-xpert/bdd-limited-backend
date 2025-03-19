package com.xpertpro.bbd_project.entity;

import com.xpertpro.bbd_project.enums.RoleEnum;
import com.xpertpro.bbd_project.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String phoneNumber;
    @Column(unique = true)
    private String email;
    private String photoUrl;
    private String password;
    private String username;
    @Enumerated(EnumType.STRING)
    private RoleEnum userRoleEnum;
    @Enumerated(EnumType.STRING)
    private Status status = Status.CREATE;
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEnum roleEnum;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt;

}
