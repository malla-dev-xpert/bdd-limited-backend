package com.xpertpro.bbd_project.entity;

import com.xpertpro.bbd_project.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "expeditions")
public class Expeditions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String expeditionType;
    private double weight;
    private double itemQuantity;
    private double cbn;
    private String ref;

    private String startCountry;
    private String destinationCountry;

    private LocalDateTime arrivalDate;
    private LocalDateTime startDate;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;

    @Enumerated(EnumType.STRING)
    private StatusEnum status = StatusEnum.PENDING;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Partners client;

    @CreatedBy
    @ManyToOne
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;
}
