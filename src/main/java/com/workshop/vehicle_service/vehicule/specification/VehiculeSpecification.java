package com.workshop.vehicle_service.vehicule.specification;


import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import org.springframework.data.jpa.domain.Specification;

public class VehiculeSpecification {

    public static Specification<Vehicule> hasImmatriculation(
            String immatriculation) {

        return (root, query, cb) -> {

            if (immatriculation == null || immatriculation.isBlank()) {
                return null;
            }

            return cb.like(
                    cb.lower(root.get("immatriculationFictive")),
                    "%" + immatriculation.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<Vehicule> hasMarque(
            String marque) {

        return (root, query, cb) -> {

            if (marque == null || marque.isBlank()) {
                return null;
            }

            return cb.like(
                    cb.lower(root.get("marque")),
                    "%" + marque.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<Vehicule> hasModele(
            String modele) {

        return (root, query, cb) -> {

            if (modele == null || modele.isBlank()) {
                return null;
            }

            return cb.like(
                    cb.lower(root.get("modele")),
                    "%" + modele.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<Vehicule> hasAnnee(
            Integer annee) {

        return (root, query, cb) -> {

            if (annee == null) {
                return null;
            }

            return cb.equal(
                    root.get("annee"),
                    annee
            );
        };
    }

    public static Specification<Vehicule> hasClientFictif(
            String clientFictif) {

        return (root, query, cb) -> {

            if (clientFictif == null || clientFictif.isBlank()) {
                return null;
            }

            return cb.like(
                    cb.lower(root.get("clientFictif")),
                    "%" + clientFictif.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<Vehicule> hasActif(
            Boolean actif) {

        return (root, query, cb) -> {

            if (actif == null) {
                return null;
            }

            return cb.equal(
                    root.get("actif"),
                    actif
            );
        };
    }
}
