package com.workshop.vehicle_service.vehicule.entity;

import com.workshop.vehicle_service.intervention.entity.Intervention;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "immatriculation_fictive", nullable = false, unique = true)
    private String immatriculationFictive;

    @Column(nullable = false)
    private String marque;

    @Column(nullable = false)
    private String modele;

    @Column(nullable = false)
    private Integer annee;

    @Column(nullable = false)
    private Long kilometrage;

    @Column(name = "client_fictif", nullable = false)
    private String clientFictif;

    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    @Builder.Default
    @OneToMany(mappedBy = "vehicule")
    private List<Intervention> interventions = new ArrayList<>();
}
