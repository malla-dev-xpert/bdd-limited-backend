package com.xpertpro.bbd_project.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity @Table(name = "warehouse")
public class Warehouse {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String adresse;
    private String storageType;

    @Enumerated(EnumType.STRING)
    private StatusEnum status = StatusEnum.CREATE;

    private LocalDateTime createdAt;
    private LocalDateTime editedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Packages> packages = new ArrayList<>();
}
