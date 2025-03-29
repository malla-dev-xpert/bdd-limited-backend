package com.xpertpro.bbd_project.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity @Table(name = "carriers")
public class Carriers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String contact;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<CarrierServiceEntity> carrierService = new ArrayList<>();;

    @Enumerated(EnumType.STRING)
    private StatusEnum status = StatusEnum.CREATE;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
