package com.xpertpro.bbd_project.entity;


import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity @Table(name = "category")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    @Enumerated(EnumType.STRING)
    private StatusEnum status = StatusEnum.CREATE;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
