package com.workshop.vehicle_service.intervention.service;

import com.workshop.vehicle_service.common.dto.JourCompte;
import com.workshop.vehicle_service.intervention.dto.InterventionCreateRequest;
import com.workshop.vehicle_service.intervention.dto.InterventionResponse;
import com.workshop.vehicle_service.intervention.dto.InterventionUpdateRequest;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.enums.TypeIntervention;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InterventionService {

    InterventionResponse create(InterventionCreateRequest request);

    InterventionResponse findByNumero(String numero);

    Page<InterventionResponse> list(Pageable pageable);

    InterventionResponse update(String numero, InterventionUpdateRequest request);

    void delete(String numero);

    Page<InterventionResponse> findAutresInterventionsDuVehicule(String numero, Pageable pageable);

    // --- Lecture agrégée pour le dashboard (§3.6) — jamais d'écriture ---

    /** Interventions actives dont la date de dépôt est le jour calendaire courant. */
    long countRecuesAujourdHui();

    /** Interventions actives actuellement dans le statut donné. */
    long countEnStatut(StatutIntervention statut);

    /**
     * Interventions actives en retard : date de restitution prévue dépassée ET
     * statut ∉ {RESTITUEE, ANNULEE}. Règle figée ici (propriétaire de la
     * donnée), jamais dupliquée côté dashboard ou front.
     */
    long countRetards();

    /** Liste paginée des interventions en retard (même règle que {@link #countRetards()}). */
    Page<InterventionResponse> findRetards(Pageable pageable);

    /** Répartition du nombre d'interventions actives par statut (clés présentes uniquement si total > 0). */
    Map<StatutIntervention, Long> countParStatut();

    /** Répartition du nombre d'interventions actives par type (clés présentes uniquement si total > 0). */
    Map<TypeIntervention, Long> countParType();

    /**
     * Charge active (statut ∉ {TERMINEE, RESTITUEE, ANNULEE}) par mécanicien,
     * indexée par id technique du mécanicien. Mécaniciens sans intervention
     * active absents de la map (charge implicite = 0).
     */
    Map<Long, Long> chargeActiveParMecanicien();

    /** Série journalière des interventions reçues sur [debut, finInclusive], un point par jour, sans trou. */
    List<JourCompte> volumeRecuesParJour(LocalDate debut, LocalDate finInclusive);
}
