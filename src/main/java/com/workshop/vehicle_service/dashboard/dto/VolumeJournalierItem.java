package com.workshop.vehicle_service.dashboard.dto;

import java.time.LocalDate;

/** Point journalier de la série « reçues vs terminées » (graphique volume dans le temps). */
public record VolumeJournalierItem(LocalDate date, long recues, long terminees) {
}
