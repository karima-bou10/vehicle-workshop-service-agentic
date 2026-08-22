package com.workshop.vehicle_service.intervention.repository;

import com.workshop.vehicle_service.intervention.dto.InterventionListFilter;
import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.time.LocalDateTime;
import java.util.Collection;
import org.springframework.data.jpa.domain.Specification;

public final class InterventionSpecifications {

    private InterventionSpecifications() {
    }

    public static Specification<Intervention> build(InterventionListFilter filter, LocalDateTime now,
            Collection<StatutIntervention> statutsExclusRetard) {
        return Specification.where(activeOnly())
                .and(byStatut(filter.statut()))
                .and(byMecanicien(filter.mecanicienId()))
                .and(byVehicule(filter.vehiculeId()))
                .and(byVehiculeTexte(filter.immatriculation()))
                .and(byRechercheLibre(filter.q()))
                .and(byRetard(filter.enRetard(), now, statutsExclusRetard));
    }

    public static Specification<Intervention> activeOnly() {
        return (root, query, cb) -> cb.isTrue(root.get("actif"));
    }

    public static Specification<Intervention> byStatut(StatutIntervention statut) {
        return (root, query, cb) -> statut == null ? null : cb.equal(root.get("statut"), statut);
    }

    public static Specification<Intervention> byMecanicien(Long mecanicienId) {
        return (root, query, cb) -> mecanicienId == null ? null
                : cb.equal(root.join("mecanicien", JoinType.LEFT).get("id"), mecanicienId);
    }

    public static Specification<Intervention> byVehicule(Long vehiculeId) {
        return (root, query, cb) -> vehiculeId == null ? null
                : cb.equal(root.join("vehicule", JoinType.INNER).get("id"), vehiculeId);
    }

    public static Specification<Intervention> byVehiculeTexte(String immatriculation) {
        return (root, query, cb) -> {
            if (immatriculation == null) {
                return null;
            }

            query.distinct(true);
            Join<Intervention, Vehicule> vehicule = root.join("vehicule", JoinType.INNER);
            String like = "%" + immatriculation.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(vehicule.get("immatriculationFictive")), like),
                    cb.like(cb.lower(vehicule.get("marque")), like),
                    cb.like(cb.lower(vehicule.get("modele")), like));
        };
    }

    public static Specification<Intervention> byRechercheLibre(String q) {
        return (root, query, cb) -> {
            if (q == null) {
                return null;
            }

            String like = "%" + q.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("numero")), like),
                    cb.like(cb.lower(root.get("descriptionClient")), like));
        };
    }

    public static Specification<Intervention> byRetard(Boolean enRetard, LocalDateTime now,
            Collection<StatutIntervention> statutsExclusRetard) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(enRetard)) {
                return null;
            }

            return cb.and(
                    cb.isNotNull(root.get("dateRestitutionPrevue")),
                    cb.lessThan(root.get("dateRestitutionPrevue"), now),
                    cb.not(root.get("statut").in(statutsExclusRetard)));
        };
    }

    public static boolean isEnRetard(Intervention intervention, LocalDateTime now,
            Collection<StatutIntervention> statutsExclusRetard) {
        return intervention.getDateRestitutionPrevue() != null
                && intervention.getDateRestitutionPrevue().isBefore(now)
                && !statutsExclusRetard.contains(intervention.getStatut());
    }

    public static String statutLibelle(StatutIntervention statut) {
        return switch (statut) {
            case RECUE -> "Reçue";
            case DIAGNOSTIC_EN_COURS -> "Diagnostic en cours";
            case DEVIS_A_VALIDER -> "Devis à valider";
            case EN_REPARATION -> "En réparation";
            case TERMINEE -> "Terminée";
            case RESTITUEE -> "Restituée";
            case ANNULEE -> "Annulée";
        };
    }
}
