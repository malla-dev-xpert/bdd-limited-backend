package com.xpertpro.bbd_project.entity;

import com.xpertpro.bbd_project.enums.PermissionsEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity @Data @AllArgsConstructor @NoArgsConstructor
@Table(name = "roles")
public class RolesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;  // Le nom du rôle (ex. : Gestionnaire Logistique)

//    @ElementCollection(fetch = FetchType.EAGER)
//    @Enumerated(EnumType.STRING)
//    private Set<PermissionsEnum> permissions = new HashSet<>();  // Permissions liées au rôle sous forme d'énumérations

    @ElementCollection(targetClass = PermissionsEnum.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "role_permissions", joinColumns = @JoinColumn(name = "role_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Set<PermissionsEnum> permissions;

//    @OneToMany(mappedBy = "roleName", cascade = CascadeType.ALL)
//    private List<UserEntity> users = new ArrayList<>();
}
