package com.workshop.vehicle_service.intervention.entity;

import com.workshop.vehicle_service.historique.entity.HistoriqueIntervention;
import com.workshop.vehicle_service.intervention.enums.PrioriteIntervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.enums.TypeIntervention;
import com.workshop.vehicle_service.mecanicien.entity.Mecanicien;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "intervention")
@EntityListeners(InterventionEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mecanicien_id")
    private Mecanicien mecanicien;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeIntervention type;

    @Column(name = "description_client", nullable = false, columnDefinition = "TEXT")
    private String descriptionClient;

    @Column(columnDefinition = "TEXT")
    private String diagnostic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutIntervention statut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioriteIntervention priorite;

    @Column(name = "cout_estime", precision = 12, scale = 2)
    private BigDecimal coutEstime;

    @Column(name = "date_depot", nullable = false)
    private LocalDateTime dateDepot;

    @Column(name = "date_restitution_prevue")
    private LocalDateTime dateRestitutionPrevue;

    @Column(name = "date_cloture")
    private LocalDateTime dateCloture;

    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    @Builder.Default
    @OneToMany(mappedBy = "intervention", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistoriqueIntervention> historiques = new ArrayList<>();
}
