package com.xpertpro.bbd_project.entity;


import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity @Table(name = "conatiners")
public class Containers {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String reference;
    @Enumerated(EnumType.STRING)
    private StatusEnum status = StatusEnum.CREATE;
    private Boolean isAvailable;

    private LocalDateTime createdAt;
    private LocalDateTime editedAt;

    @JoinColumn(name = "user_id")
    @ManyToOne
    private UserEntity user;

}
