package com.xpertpro.bbd_project.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
@Entity(name = "versements")
public class Versements {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double montantTotal;
    private Double montantRestant;

    private LocalDateTime dateVersement = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "partner_id", nullable = false)
    private Partners partner;

    @OneToMany(mappedBy = "versement", cascade = CascadeType.ALL)
    private List<Achats> achats = new ArrayList<>();
}
