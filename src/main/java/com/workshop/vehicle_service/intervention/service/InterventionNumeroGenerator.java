package com.workshop.vehicle_service.intervention.service;

import com.workshop.vehicle_service.intervention.entity.InterventionSequenceAnnuelle;
import com.workshop.vehicle_service.intervention.repository.InterventionSequenceAnnuelleRepository;
import java.time.Year;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterventionNumeroGenerator {

    private final InterventionSequenceAnnuelleRepository sequenceRepository;

    @Transactional
    public String nextNumero() {
        int annee = Year.now().getValue();

        InterventionSequenceAnnuelle sequence = sequenceRepository.findById(annee)
                .map(existing -> {
                    existing.setValeurCourante(existing.getValeurCourante() + 1);
                    return existing;
                })
                .orElseGet(() -> InterventionSequenceAnnuelle.builder()
                        .annee(annee)
                        .valeurCourante(1)
                        .build());

        InterventionSequenceAnnuelle saved = sequenceRepository.saveAndFlush(sequence);
        return "INT-%d-%06d".formatted(saved.getAnnee(), saved.getValeurCourante());
    }
}
