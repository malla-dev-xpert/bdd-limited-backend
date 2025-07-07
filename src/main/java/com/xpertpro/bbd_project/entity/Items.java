package com.xpertpro.bbd_project.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    private int quantity;
    private double unitPrice;
    private double totalPrice;
    private double salesRate;
    private String invoiceNumber;
    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "package_id")
    private Packages packages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achat_id", nullable = false)
    @JsonIgnoreProperties("items")
    private Achats achats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Partners supplier;

}
