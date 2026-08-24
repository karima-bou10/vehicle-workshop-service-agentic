package com.workshop.vehicle_service.dashboard.service;

import com.workshop.vehicle_service.dashboard.dto.DashboardKpisResponse;
import com.workshop.vehicle_service.dashboard.dto.MecanicienChargeResponse;
import com.workshop.vehicle_service.dashboard.dto.StatutRepartitionItem;
import com.workshop.vehicle_service.dashboard.dto.TypeRepartitionItem;
import com.workshop.vehicle_service.dashboard.dto.VolumeJournalierItem;
import com.workshop.vehicle_service.intervention.dto.InterventionResponse;
import com.workshop.vehicle_service.dashboard.dto.MecanicienSyntheseResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DashboardService {

    /** Compteurs KPI du jour (§ US1/US3). */
    DashboardKpisResponse getKpis();

    /** Interventions en retard, paginées, échéance la plus ancienne en premier par défaut (§ US3). */
    Page<InterventionResponse> getRetards(Pageable pageable);

    /** Charge active par mécanicien actif, triée par nom (§ US2). */
    List<MecanicienChargeResponse> getChargeMecaniciens();

    /** Répartition des interventions actives par statut, 7 éléments dans l'ordre de l'enum (§ US4). */
    List<StatutRepartitionItem> getRepartitionStatuts();

    /** Répartition des interventions actives par type, 6 éléments dans l'ordre de l'enum (§ US6). */
    List<TypeRepartitionItem> getRepartitionTypes();

    /** Série journalière « reçues vs terminées » sur les {@code jours} derniers jours calendaires, aujourd'hui inclus (§ US5). */
    List<VolumeJournalierItem> getVolume(int jours);

    /** Synthèse paginée par mécanicien actif (frontend navigation: public id, name, counts, avg delay). */
    Page<MecanicienSyntheseResponse> getSyntheseMecaniciens(Pageable pageable);
}
