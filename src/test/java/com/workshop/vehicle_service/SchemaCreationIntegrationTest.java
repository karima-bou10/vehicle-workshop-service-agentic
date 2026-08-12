package com.workshop.vehicle_service;

import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.enums.PrioriteIntervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.enums.TypeIntervention;
import com.workshop.vehicle_service.intervention.repository.InterventionRepository;
import com.workshop.vehicle_service.mecanicien.entity.Mecanicien;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import com.workshop.vehicle_service.vehicule.repository.VehiculeRepository;
import java.time.LocalDateTime;
import java.time.Year;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class SchemaCreationIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private VehiculeRepository vehiculeRepository;

    @Autowired
    private InterventionRepository interventionRepository;

    @Test
    void expectedTablesShouldExistAndInterventionNumeroShouldBeGenerated() throws Exception {
        assertTableExists("vehicule");
        assertTableExists("mecanicien");
        assertTableExists("intervention");
        assertTableExists("historique_intervention");
        assertTableExists("intervention_sequence_annuelle");

        Vehicule vehicule = vehiculeRepository.save(Vehicule.builder()
                .immatriculationFictive("TEST-123")
                .marque("Renault")
                .modele("Clio")
                .annee(2024)
                .kilometrage(120000L)
                .clientFictif("Client Demo")
                .build());

        Intervention intervention = interventionRepository.save(Intervention.builder()
                .vehicule(vehicule)
                .mecanicien((Mecanicien) null)
                .type(TypeIntervention.DIAGNOSTIC)
                .descriptionClient("Bruitage moteur")
                .statut(StatutIntervention.RECUE)
                .priorite(PrioriteIntervention.NORMALE)
                .dateDepot(LocalDateTime.now())
                .build());

        assertNotNull(intervention.getNumero());
        assertTrue(intervention.getNumero().matches("INT-" + Year.now().getValue() + "-\\d{6}"));
    }

    private void assertTableExists(String tableName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
                ResultSet resultSet = connection.getMetaData().getTables(null, null, tableName, null)) {
            assertTrue(resultSet.next(), () -> "Missing table: " + tableName);
        }
    }
}
