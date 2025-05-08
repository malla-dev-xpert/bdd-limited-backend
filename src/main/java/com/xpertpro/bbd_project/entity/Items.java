package com.xpertpro.bbd_project.entity;

import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "items")
public class Items {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String description;
    private double quantity;
    private double unitPrice;
    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    @ManyToOne
    @JoinColumn(name = "achat_id")
    private Achats achats;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "package_id")
    private Packages packages;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
