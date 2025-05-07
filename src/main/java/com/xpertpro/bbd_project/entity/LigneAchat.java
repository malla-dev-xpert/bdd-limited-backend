package com.xpertpro.bbd_project.entity;

import jakarta.persistence.*;

public class LigneAchat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "achat_id")
    private Achats achat;

    @ManyToOne
    @JoinColumn(name = "item_id") // Référence à ton entité actuelle Items
    private Items item;

    private Integer quantite;
    private Double prixTotal;
}
