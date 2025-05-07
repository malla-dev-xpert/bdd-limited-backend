package com.xpertpro.bbd_project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity(name = "ligneachats")
public class LigneAchat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "achat_id")
    private Achats achats;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Items item;

    private Integer quantite;
    private Double prixTotal;
}
