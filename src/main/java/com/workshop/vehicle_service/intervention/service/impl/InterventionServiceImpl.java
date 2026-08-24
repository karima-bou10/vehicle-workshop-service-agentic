package com.workshop.vehicle_service.intervention.service.impl;

import com.workshop.vehicle_service.common.dto.JourCompte;
import com.workshop.vehicle_service.common.exception.*;
import com.workshop.vehicle_service.intervention.dto.InterventionCreateRequest;
import com.workshop.vehicle_service.intervention.dto.InterventionResponse;
import com.workshop.vehicle_service.intervention.dto.InterventionUpdateRequest;
import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.enums.TypeIntervention;
import com.workshop.vehicle_service.intervention.mapper.InterventionMapper;
import com.workshop.vehicle_service.intervention.repository.InterventionRepository;
import com.workshop.vehicle_service.intervention.repository.InterventionSpecifications;
import com.workshop.vehicle_service.intervention.service.InterventionNumeroGenerator;
import com.workshop.vehicle_service.intervention.service.InterventionService;
import com.workshop.vehicle_service.mecanicien.service.MecanicienService;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import com.workshop.vehicle_service.vehicule.service.VehiculeService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import com.workshop.vehicle_service.intervention.dto.InterventionListFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
public class InterventionServiceImpl implements InterventionService  {

    private static final DateTimeFormatter CSV_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String CSV_SEPARATOR = ",";

    /** Statuts considérés comme "clôturés" pour la charge active d'un mécanicien. */
    private static final List<StatutIntervention> STATUTS_CLOTURES = List.of(
            StatutIntervention.TERMINEE, StatutIntervention.RESTITUEE, StatutIntervention.ANNULEE);

    /** Statuts jamais considérés en retard, même si la date de restitution prévue est dépassée. */
    private static final List<StatutIntervention> STATUTS_EXCLUS_RETARD = List.of(
            StatutIntervention.RESTITUEE, StatutIntervention.ANNULEE);

    private static final Set<StatutIntervention> STATUTS_FINAUX = Set.of(
            StatutIntervention.TERMINEE, StatutIntervention.RESTITUEE, StatutIntervention.ANNULEE);

    private final InterventionRepository interventionRepository;
    private final VehiculeService vehiculeService;
    private final MecanicienService mecanicienService;
    private final InterventionNumeroGenerator numeroGenerator;
    private final InterventionMapper interventionMapper;

    @Override
    public InterventionResponse create(InterventionCreateRequest request) {
        Vehicule vehicule = vehiculeService.findActifById(request.vehiculeId());
        if (vehicule == null) {
            throw new IllegalArgumentException("Véhicule introuvable pour l'id " + request.vehiculeId());
        }

        LocalDateTime dateDepot = request.dateDepot() != null ? request.dateDepot() : LocalDateTime.now();

        Intervention intervention = Intervention.builder()
                .numero(numeroGenerator.nextNumero())
                .vehicule(vehicule)
                .type(request.type())
                .descriptionClient(request.descriptionClient())
                .statut(StatutIntervention.RECUE)
                .priorite(request.priorite())
                .dateDepot(dateDepot)
                .actif(true)
                .build();

        Intervention saved = interventionRepository.save(intervention);
        return toResponse(saved, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public InterventionResponse findByNumero(String numero) {
        LocalDateTime evaluationTime = LocalDateTime.now();
        return toResponse(getEntityByNumero(numero), evaluationTime);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterventionResponse> list(InterventionListFilter filter, Pageable pageable) {
        validatePageable(pageable);
        LocalDateTime evaluationTime = LocalDateTime.now();
        return interventionRepository.findAll(
                InterventionSpecifications.build(filter, evaluationTime, STATUTS_EXCLUS_RETARD),
                pageable).map(intervention -> toResponse(intervention, evaluationTime));
    }

    @Override
    @Transactional(readOnly = true)
    public String exportCsv(InterventionListFilter filter) {
        LocalDateTime evaluationTime = LocalDateTime.now();
        List<Intervention> interventions = interventionRepository.findAll(
                InterventionSpecifications.build(filter, evaluationTime, STATUTS_EXCLUS_RETARD),
                org.springframework.data.domain.Sort
                        .by(org.springframework.data.domain.Sort.Direction.DESC, "dateDepot")
                        .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC,
                                "numero")));

        StringBuilder csv = new StringBuilder();
        csv.append(
                        "numero,immatriculation,marque,modele,type,statut,priorite,dateDepot,dateRestitutionPrevue,mecanicien,enRetard")
                .append(System.lineSeparator());

        for (Intervention intervention : interventions) {
            boolean enRetard = InterventionSpecifications.isEnRetard(intervention, evaluationTime,
                    STATUTS_EXCLUS_RETARD);
            csv.append(csvValue(intervention.getNumero())).append(CSV_SEPARATOR)
                    .append(csvValue(intervention.getVehicule().getImmatriculationFictive())).append(CSV_SEPARATOR)
                    .append(csvValue(intervention.getVehicule().getMarque())).append(CSV_SEPARATOR)
                    .append(csvValue(intervention.getVehicule().getModele())).append(CSV_SEPARATOR)
                    .append(csvValue(typeLibelle(intervention.getType()))).append(CSV_SEPARATOR)
                    .append(csvValue(InterventionSpecifications.statutLibelle(intervention.getStatut())))
                    .append(CSV_SEPARATOR)
                    .append(csvValue(prioriteLibelle(intervention.getPriorite().name()))).append(CSV_SEPARATOR)
                    .append(csvValue(formatDateTime(intervention.getDateDepot()))).append(CSV_SEPARATOR)
                    .append(csvValue(formatDateTime(intervention.getDateRestitutionPrevue()))).append(CSV_SEPARATOR)
                    .append(csvValue(
                            intervention.getMecanicien() != null ? intervention.getMecanicien().getNom() : null))
                    .append(CSV_SEPARATOR)
                    .append(csvValue(enRetard ? "oui" : "non"))
                    .append(System.lineSeparator());
        }

        return csv.toString();
    }

    @Override
    public InterventionResponse update(String numero, InterventionUpdateRequest request) {
        Intervention intervention = getEntityByNumero(numero);
        if (!intervention.isActif()) {
            throw new InterventionInactiveException(
                    "Intervention désactivée, modification impossible pour le numero " + numero);
        }
        validateUpdateAllowedByStatus(intervention, request);

        intervention.setType(request.type());
        intervention.setDescriptionClient(request.descriptionClient());
        intervention.setPriorite(request.priorite());
        intervention.setDateDepot(request.dateDepot());
        intervention.setCoutEstime(request.coutEstime());
        intervention.setDateRestitutionPrevue(request.dateRestitutionPrevue());
        intervention.setDiagnostic(request.diagnostic());

        Intervention saved = interventionRepository.save(intervention);
        return toResponse(saved, LocalDateTime.now());
    }

    @Override
    public void delete(String numero) {
        Intervention intervention = getEntityByNumero(numero);
        if (!isArchivedStatus(intervention.getStatut())) {
            throw new ArchivageNonAutoriseException(
                    "Archivage impossible pour une intervention au statut " + intervention.getStatut());
        }
        if (intervention.isActif()) {
            intervention.setActif(false);
            interventionRepository.save(intervention);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterventionResponse> findAutresInterventionsDuVehicule(String numero, Pageable pageable) {
        validatePageable(pageable);
        Intervention sourceIntervention = getEntityByNumero(numero);
        return interventionRepository
                .findByVehiculeIdAndIdNotAndActifTrue(sourceIntervention.getVehicule().getId(),
                        sourceIntervention.getId(),
                        pageable)
                .map(intervention -> toResponse(intervention, LocalDateTime.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public long countRecuesAujourdHui() {
        LocalDateTime debut = LocalDate.now().atStartOfDay();
        LocalDateTime fin = debut.plusDays(1);
        return interventionRepository.countByActifTrueAndDateDepotGreaterThanEqualAndDateDepotLessThan(debut, fin);
    }

    @Override
    @Transactional(readOnly = true)
    public long countEnStatut(StatutIntervention statut) {
        return interventionRepository.countByActifTrueAndStatut(statut);
    }

    @Override
    @Transactional(readOnly = true)
    public long countRetards() {
        return interventionRepository.countByActifTrueAndDateRestitutionPrevueBeforeAndStatutNotIn(
                LocalDateTime.now(), STATUTS_EXCLUS_RETARD);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterventionResponse> findRetards(Pageable pageable) {
        validatePageable(pageable);
        return interventionRepository
                .findByActifTrueAndDateRestitutionPrevueBeforeAndStatutNotIn(LocalDateTime.now(),
                        STATUTS_EXCLUS_RETARD, pageable)
                .map(intervention -> toResponse(intervention, LocalDateTime.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<StatutIntervention, Long> countParStatut() {
        return interventionRepository.countActifsGroupeParStatut().stream()
                .collect(Collectors.toMap(InterventionRepository.StatutCount::getStatut,
                        InterventionRepository.StatutCount::getTotal));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<TypeIntervention, Long> countParType() {
        return interventionRepository.countActifsGroupeParType().stream()
                .collect(Collectors.toMap(InterventionRepository.TypeCount::getType,
                        InterventionRepository.TypeCount::getTotal));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> chargeActiveParMecanicien() {
        return interventionRepository.chargeActiveParMecanicien(STATUTS_CLOTURES).stream()
                .collect(Collectors.toMap(InterventionRepository.MecanicienCharge::getMecanicienId,
                        InterventionRepository.MecanicienCharge::getTotal));
    }

    @Override
    @Transactional(readOnly = true)
    public List<JourCompte> volumeRecuesParJour(LocalDate debut, LocalDate finInclusive) {
        LocalDateTime debutDateTime = debut.atStartOfDay();
        LocalDateTime finExclusiveDateTime = finInclusive.plusDays(1).atStartOfDay();
        Map<LocalDate, Long> parJour = interventionRepository
                .findDateDepotDansPeriode(debutDateTime, finExclusiveDateTime).stream()
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));

        List<JourCompte> serie = new ArrayList<>();
        for (LocalDate jour = debut; !jour.isAfter(finInclusive); jour = jour.plusDays(1)) {
            serie.add(new JourCompte(jour, parJour.getOrDefault(jour, 0L)));
        }
        return serie;
    }

    private Intervention getEntityByNumero(String numero) {
        return interventionRepository.findByNumero(numero)
                .orElseThrow(() -> new InterventionIntrouvableException(
                        "Intervention introuvable pour le numero " + numero));
    }

    private InterventionResponse toResponse(Intervention intervention, LocalDateTime evaluationTime) {
        return interventionMapper.toResponse(intervention,
                InterventionSpecifications.isEnRetard(intervention, evaluationTime, STATUTS_EXCLUS_RETARD));
    }

    private void validateUpdateAllowedByStatus(Intervention intervention, InterventionUpdateRequest request) {
        StatutIntervention statut = intervention.getStatut();
        if (statut == StatutIntervention.RESTITUEE || statut == StatutIntervention.ANNULEE) {
            throw new ModificationInterventionNonAutoriseeException(
                    "Modification interdite pour une intervention au statut " + statut);
        }

        if (statut != StatutIntervention.RECUE && !Objects.equals(request.type(), intervention.getType())) {
            throw new ModificationInterventionNonAutoriseeException(
                    "Le champ type est modifiable uniquement au statut RECUE");
        }

        if ((statut == StatutIntervention.EN_REPARATION || statut == StatutIntervention.TERMINEE)
                && !Objects.equals(request.descriptionClient(), intervention.getDescriptionClient())) {
            throw new ModificationInterventionNonAutoriseeException(
                    "Le champ descriptionClient est modifiable jusqu'au statut DEVIS_A_VALIDER");
        }

        if ((statut == StatutIntervention.TERMINEE)
                && !Objects.equals(request.priorite(), intervention.getPriorite())) {
            throw new ModificationInterventionNonAutoriseeException(
                    "Le champ priorite est modifiable jusqu'au statut EN_REPARATION");
        }

        if (statut != StatutIntervention.RECUE && !Objects.equals(request.dateDepot(), intervention.getDateDepot())) {
            throw new ModificationInterventionNonAutoriseeException(
                    "Le champ dateDepot est modifiable uniquement au statut RECUE");
        }
        if (statut != StatutIntervention.DEVIS_A_VALIDER
                && !Objects.equals(request.dateRestitutionPrevue(), intervention.getDateRestitutionPrevue())) {
            throw new ModificationInterventionNonAutoriseeException(
                    "Le champ dateRestitutionPrevue est modifiable uniquement au statut devis à valider");
        }

        if (statut != StatutIntervention.DEVIS_A_VALIDER
                && !Objects.equals(request.coutEstime(), intervention.getCoutEstime())) {
            throw new ModificationInterventionNonAutoriseeException(
                    "Le champ coutEstime est modifiable uniquement au statut devis à valider");
        }
        if (statut != StatutIntervention.DIAGNOSTIC_EN_COURS && statut != StatutIntervention.DEVIS_A_VALIDER
                && !Objects.equals(request.diagnostic(), intervention.getDiagnostic())) {
            throw new ModificationInterventionNonAutoriseeException(
                    "Le champ diagnostic est modifiable uniquement au statut devis à valider et diagnostic en cours");
        }
    }

    private void validatePageable(Pageable pageable) {
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1 || pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Paramètres de pagination invalides");
        }
    }

    private boolean isArchivedStatus(StatutIntervention statut) {
        return statut == StatutIntervention.RESTITUEE || statut == StatutIntervention.ANNULEE;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : CSV_DATE_TIME_FORMATTER.format(value);
    }

    private String csvValue(String value) {
        if (value == null)
            return "";

        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(CSV_SEPARATOR) || escaped.contains("\"") || escaped.contains("\n")
                || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private void validateDates(LocalDateTime dateDepot, LocalDateTime dateRestitutionPrevue) {
        if (dateDepot != null && dateRestitutionPrevue != null && dateRestitutionPrevue.isBefore(dateDepot)) {
            throw new DateRestitutionInvalideException(
                    "dateRestitutionPrevue doit être postérieure ou égale à dateDepot");
        }
    }

    private String typeLibelle(TypeIntervention type) {
        return switch (type) {
            case DIAGNOSTIC -> "Diagnostic";
            case REVISION -> "Révision";
            case REPARATION -> "Réparation";
            case CONTROLE -> "Contrôle";
            case PNEUMATIQUES -> "Pneumatiques";
            case AUTRE -> "Autre";
        };
    }

    private String prioriteLibelle(String priorite) {
        return switch (priorite) {
            case "BASSE" -> "Basse";
            case "NORMALE" -> "Normale";
            case "HAUTE" -> "Haute";
            case "URGENTE" -> "Urgente";
            default -> priorite;
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterventionResponse> findByMecanicien(Long mecanicienId, Pageable pageable) {
        validatePageable(pageable);
        // Vérifie l'existence du mécanicien (actif ou non) — 404 sinon.
        mecanicienService.findById(mecanicienId);
        return interventionRepository.findByMecanicienId(mecanicienId, pageable)
                .map(interventionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasInterventionsActivesNonFinales(Long mecanicienId) {
        Page<Intervention> page = interventionRepository.findByMecanicienId(mecanicienId, Pageable.ofSize(1));
        return page.stream().anyMatch(i -> i.isActif() && !STATUTS_CLOTURES.contains(i.getStatut()));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countRetardsParMecanicien() {
        return interventionRepository.countRetardsParMecanicien(LocalDateTime.now(), STATUTS_EXCLUS_RETARD).stream()
                .collect(Collectors.toMap(InterventionRepository.MecanicienCharge::getMecanicienId,
                        InterventionRepository.MecanicienCharge::getTotal));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Map<StatutIntervention, Long>> countActifsGroupeParMecanicienEtStatut() {
        return interventionRepository.countActifsGroupeParMecanicienEtStatut().stream()
                .collect(Collectors.groupingBy(InterventionRepository.MecanicienStatutCount::getMecanicienId,
                        Collectors.toMap(InterventionRepository.MecanicienStatutCount::getStatut,
                                InterventionRepository.MecanicienStatutCount::getTotal)));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Double> delaiMoyenTraitementParMecanicien() {
        return interventionRepository.moyenneDelaiTraitementParMecanicien(StatutIntervention.RESTITUEE.name()).stream()
            .collect(Collectors.toMap(InterventionRepository.MecanicienAvgDelay::getMecanicienId,
                d -> d.getAvgSeconds() == null ? null : d.getAvgSeconds() / 3600.0));
    }
}
