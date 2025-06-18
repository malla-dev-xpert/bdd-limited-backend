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
@Entity(name = "achats")
public class Achats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private Double montantTotal;
    private Double tauxUtilise;

    @Enumerated(EnumType.STRING)
    private StatusEnum status;

    @OneToMany(mappedBy = "achats", cascade = CascadeType.ALL)
    private List<Items> items = new ArrayList<>();
    @ManyToOne
    @JoinColumn(name = "versement_id", nullable = false)
    private Versements versement;
//    @ManyToOne
//    @JoinColumn(name = "fournisseur_id")
//    private Partners fournisseur;
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Partners client;
    @ManyToOne
    @JoinColumn(name = "devise_id")
    private Devises devise;
}
