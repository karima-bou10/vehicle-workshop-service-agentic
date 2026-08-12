package com.workshop.vehicle_service.mecanicien.entity;

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
@Table(name = "mecanicien")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mecanicien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String specialite;

    @Builder.Default
    @Column(nullable = false)
    private boolean disponible = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    @Builder.Default
    @OneToMany(mappedBy = "mecanicien")
    private List<Intervention> interventions = new ArrayList<>();
}
