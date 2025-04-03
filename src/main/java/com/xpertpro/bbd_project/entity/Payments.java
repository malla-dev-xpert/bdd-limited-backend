package com.xpertpro.bbd_project.entity;

import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity @Table(name = "payments")
public class Payments {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double amount;
    @Column(unique = false)
    private String paymentMethod;
    @ManyToOne @JoinColumn(name = "devises_id")
    private Devises devises;
    @ManyToOne @JoinColumn(name = "customer_id")
    private Partners customer;

    private Boolean isPaid = false;

    @ManyToOne @JoinColumn(name = "user_id")
    private UserEntity user;

    private LocalDateTime createdAt;
    private LocalDateTime editedAt;

    @Enumerated(EnumType.STRING)
    private StatusEnum status = StatusEnum.CREATE;
}
