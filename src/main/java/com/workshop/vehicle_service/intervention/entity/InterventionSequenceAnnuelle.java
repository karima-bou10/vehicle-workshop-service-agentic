package com.workshop.vehicle_service.intervention.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "intervention_sequence_annuelle")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterventionSequenceAnnuelle {

    @Id
    @Column(nullable = false)
    private Integer annee;

    @Column(name = "valeur_courante", nullable = false)
    private Integer valeurCourante;

    @Version
    private Long version;
}
