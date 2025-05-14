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
@Entity @Table(name = "containers")
public class Containers {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String reference;
    private String size;

    @Enumerated(EnumType.STRING)
    private StatusEnum status = StatusEnum.PENDING;

    private Boolean isAvailable;

    private LocalDateTime createdAt;
    private LocalDateTime editedAt;

    @JoinColumn(name = "user_id")
    @ManyToOne
    private UserEntity user;

    @OneToMany(mappedBy = "container", cascade = CascadeType.ALL)
    private List<Packages> packages = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "harbor_id")
    private Harbor harbor;
}
