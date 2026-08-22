package com.workshop.vehicle_service.dashboard.service.impl;

import com.workshop.vehicle_service.common.dto.JourCompte;
import com.workshop.vehicle_service.dashboard.dto.DashboardKpisResponse;
import com.workshop.vehicle_service.dashboard.dto.MecanicienChargeResponse;
import com.workshop.vehicle_service.dashboard.dto.StatutRepartitionItem;
import com.workshop.vehicle_service.dashboard.dto.TypeRepartitionItem;
import com.workshop.vehicle_service.dashboard.dto.VolumeJournalierItem;
import com.workshop.vehicle_service.dashboard.service.DashboardService;
import com.workshop.vehicle_service.historique.service.HistoriqueInterventionService;
import com.workshop.vehicle_service.intervention.dto.InterventionResponse;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.enums.TypeIntervention;
import com.workshop.vehicle_service.intervention.service.InterventionService;
import com.workshop.vehicle_service.mecanicien.dto.MecanicienResponse;
import com.workshop.vehicle_service.mecanicien.service.MecanicienService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int JOURS_MIN = 1;
    private static final int JOURS_MAX = 366;

    private final InterventionService interventionService;
    private final MecanicienService mecanicienService;
    private final HistoriqueInterventionService historiqueInterventionService;

    @Override
    public DashboardKpisResponse getKpis() {
        return new DashboardKpisResponse(
                interventionService.countRecuesAujourdHui(),
                interventionService.countEnStatut(StatutIntervention.DIAGNOSTIC_EN_COURS),
                interventionService.countEnStatut(StatutIntervention.EN_REPARATION),
                interventionService.countEnStatut(StatutIntervention.TERMINEE),
                interventionService.countRetards());
    }

    @Override
    public Page<InterventionResponse> getRetards(Pageable pageable) {
        return interventionService.findRetards(pageable);
    }

    @Override
    public List<MecanicienChargeResponse> getChargeMecaniciens() {
        Map<Long, Long> charge = interventionService.chargeActiveParMecanicien();
        List<MecanicienResponse> mecaniciensActifs = mecanicienService.listActifs();

        List<MecanicienChargeResponse> resultat = new ArrayList<>(mecaniciensActifs.size());
        for (MecanicienResponse mecanicien : mecaniciensActifs) {
            resultat.add(new MecanicienChargeResponse(
                    mecanicien.id(),
                    mecanicien.nom(),
                    mecanicien.specialite(),
                    mecanicien.disponible(),
                    charge.getOrDefault(mecanicien.id(), 0L)));
        }
        return resultat;
    }

    @Override
    public List<StatutRepartitionItem> getRepartitionStatuts() {
        Map<StatutIntervention, Long> parStatut = interventionService.countParStatut();
        List<StatutRepartitionItem> resultat = new ArrayList<>(StatutIntervention.values().length);
        for (StatutIntervention statut : StatutIntervention.values()) {
            resultat.add(new StatutRepartitionItem(statut, parStatut.getOrDefault(statut, 0L)));
        }
        return resultat;
    }

    @Override
    public List<TypeRepartitionItem> getRepartitionTypes() {
        Map<TypeIntervention, Long> parType = interventionService.countParType();
        List<TypeRepartitionItem> resultat = new ArrayList<>(TypeIntervention.values().length);
        for (TypeIntervention type : TypeIntervention.values()) {
            resultat.add(new TypeRepartitionItem(type, parType.getOrDefault(type, 0L)));
        }
        return resultat;
    }

    @Override
    public List<VolumeJournalierItem> getVolume(int jours) {
        if (jours < JOURS_MIN || jours > JOURS_MAX) {
            throw new IllegalArgumentException("jours doit être compris entre 1 et 366");
        }

        LocalDate aujourdHui = LocalDate.now();
        LocalDate debut = aujourdHui.minusDays(jours - 1L);

        List<JourCompte> recues = interventionService.volumeRecuesParJour(debut, aujourdHui);
        List<JourCompte> terminees = historiqueInterventionService.volumeTermineesParJour(debut, aujourdHui);

        List<VolumeJournalierItem> serie = new ArrayList<>(recues.size());
        for (int i = 0; i < recues.size(); i++) {
            serie.add(new VolumeJournalierItem(recues.get(i).date(), recues.get(i).total(), terminees.get(i).total()));
        }
        return serie;
    }
}
