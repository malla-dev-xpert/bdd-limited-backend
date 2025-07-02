package com.xpertpro.bbd_project.entity;

import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity @Table(name = "retrait-argent")
public class CashWithdrawal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double montant;
    private LocalDateTime dateRetrait = LocalDateTime.now();
    private String note;

    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private Partners partner;
    @ManyToOne
    @JoinColumn(name = "versement_id", nullable = false)
    private Versements versement;
    @ManyToOne
    @JoinColumn(name = "devise_id", nullable = false)
    private Devises devise;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;
}
