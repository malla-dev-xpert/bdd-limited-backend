package com.xpertpro.bbd_project.entity;

import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity @Table(name = "partners")
public class Partners {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    @Column(unique = true)
    private String phoneNumber;
    private String email;

    private String country;
    private String adresse;

    @Enumerated(EnumType.STRING)
    private StatusEnum status = StatusEnum.CREATE;

    private String accountType;
    private Double balance = 0.0; //solde du client

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime editedAt;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Packages> packages = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "partner", cascade = CascadeType.ALL)
    private List<Versements> versements = new ArrayList<>();

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Expeditions> expeditions = new ArrayList<>();
}
