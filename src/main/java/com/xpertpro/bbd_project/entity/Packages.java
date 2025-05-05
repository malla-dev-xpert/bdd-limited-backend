package com.xpertpro.bbd_project.entity;

import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "packages")
public class Packages {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String reference;

    private Double weight;
    private String dimensions;

    private LocalDateTime createdAt;
    private LocalDateTime editedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private StatusEnum status = StatusEnum.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id")
    private Containers container;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private Partners partner;

    @OneToMany(mappedBy = "packages", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Items> items = new ArrayList<>();
}
