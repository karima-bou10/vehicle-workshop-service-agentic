package com.workshop.vehicle_service.historique.entity;

import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "historique_intervention")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueIntervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "intervention_id", nullable = false)
    private Intervention intervention;

    @Enumerated(EnumType.STRING)
    @Column(name = "ancien_statut", nullable = false)
    private StatutIntervention ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(name = "nouveau_statut", nullable = false)
    private StatutIntervention nouveauStatut;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(nullable = false)
    private String auteur;

    @Column(nullable = false)
    private LocalDateTime date;
}
