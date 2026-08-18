package com.workshop.vehicle_service.intervention;

import com.workshop.vehicle_service.auth.AuthIntegrationTestBase;
import com.workshop.vehicle_service.intervention.entity.Intervention;
import com.workshop.vehicle_service.intervention.enums.PrioriteIntervention;
import com.workshop.vehicle_service.intervention.enums.StatutIntervention;
import com.workshop.vehicle_service.intervention.enums.TypeIntervention;
import com.workshop.vehicle_service.intervention.repository.InterventionRepository;
import com.workshop.vehicle_service.mecanicien.entity.Mecanicien;
import com.workshop.vehicle_service.mecanicien.repository.MecanicienRepository;
import com.workshop.vehicle_service.vehicule.entity.Vehicule;
import com.workshop.vehicle_service.vehicule.repository.VehiculeRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Couvre : création avec/sans véhicule, numero unique/bien formaté (RG-AUTO-01,
 * RG-AUTO-08),
 * consultation par numero, soft delete (RG-AUTO-09).
 */
class InterventionControllerIntegrationTest extends AuthIntegrationTestBase {

        @Autowired
        private VehiculeRepository vehiculeRepository;

        @Autowired
        private InterventionRepository interventionRepository;

        @Autowired
        private MecanicienRepository mecanicienRepository;

        private Vehicule activeVehicule() {
                String immat = "AB-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase() + "-CD";
                return vehiculeRepository.save(Vehicule.builder()
                                .immatriculationFictive(immat)
                                .marque("Renault")
                                .modele("Clio")
                                .annee(2020)
                                .kilometrage(50000L)
                                .clientFictif("Client Test")
                                .actif(true)
                                .build());
        }

        private Vehicule inactiveVehicule() {
                return vehiculeRepository.save(Vehicule.builder()
                                .immatriculationFictive("XY-999-ZZ")
                                .marque("Peugeot")
                                .modele("208")
                                .annee(2018)
                                .kilometrage(90000L)
                                .clientFictif("Client Inactif")
                                .actif(false)
                                .build());
        }

        @BeforeEach
        void cleanInterventions() {
                interventionRepository.deleteAll();
                mecanicienRepository.deleteAll();
                vehiculeRepository.deleteAll();
        }

        private Mecanicien activeMecanicien() {
                return mecanicienRepository.save(Mecanicien.builder()
                                .nom("Mec Test")
                                .specialite("Moteur")
                                .disponible(true)
                                .actif(true)
                                .build());
        }

        private String createPayload(Long vehiculeId) {
                return "{\"vehiculeId\":" + vehiculeId + ",\"type\":\"REPARATION\","
                                + "\"descriptionClient\":\"Bruit au freinage\",\"priorite\":\"NORMALE\"}";
        }

        @Test
        void createWithActiveVehiculeShouldReturn201WithGeneratedNumeroAndStatutRecue() throws Exception {
                String token = loginAndGetToken("user1", "pass123");
                Vehicule vehicule = activeVehicule();

                mockMvc.perform(post("/api/interventions")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPayload(vehicule.getId())))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.numero", matchesPattern("INT-\\d{4}-\\d{6}")))
                                .andExpect(jsonPath("$.statut").value("RECUE"))
                                .andExpect(jsonPath("$.vehicule.immatriculationFictive",
                                                matchesPattern("AB-[A-F0-9]{6}-CD")));
        }

        @Test
        void createWithoutVehiculeIdShouldReturn400() throws Exception {
                String token = loginAndGetToken("user1", "pass123");

                String payload = "{\"type\":\"REPARATION\",\"descriptionClient\":\"Bruit\",\"priorite\":\"NORMALE\"}";

                mockMvc.perform(post("/api/interventions")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void createWithUnknownVehiculeShouldReturn404() throws Exception {
                String token = loginAndGetToken("user1", "pass123");

                mockMvc.perform(post("/api/interventions")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPayload(99999L)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void createWithInactiveVehiculeShouldReturn422() throws Exception {
                String token = loginAndGetToken("user1", "pass123");
                Vehicule vehicule = inactiveVehicule();

                mockMvc.perform(post("/api/interventions")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPayload(vehicule.getId())))
                                .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void createTwiceShouldGenerateTwoDistinctNumeros() throws Exception {
                String token = loginAndGetToken("user1", "pass123");
                Vehicule vehicule = activeVehicule();

                String first = mockMvc.perform(post("/api/interventions")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPayload(vehicule.getId())))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                String second = mockMvc.perform(post("/api/interventions")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPayload(vehicule.getId())))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                org.junit.jupiter.api.Assertions.assertNotEquals(first, second);
        }

        @Test
        void getByNumeroShouldReturnInterventionWhenExists() throws Exception {
                String token = loginAndGetToken("user1", "pass123");
                Vehicule vehicule = activeVehicule();
                Intervention intervention = interventionRepository.save(Intervention.builder()
                                .numero("INT-2026-000001")
                                .vehicule(vehicule)
                                .type(TypeIntervention.DIAGNOSTIC)
                                .descriptionClient("Vibration au volant")
                                .statut(StatutIntervention.RECUE)
                                .priorite(PrioriteIntervention.BASSE)
                                .dateDepot(LocalDateTime.now())
                                .actif(true)
                                .build());

                mockMvc.perform(get("/api/interventions/{numero}", intervention.getNumero())
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.numero").value("INT-2026-000001"));
        }

        @Test
        void getByNumeroShouldReturn404WhenUnknown() throws Exception {
                String token = loginAndGetToken("user1", "pass123");

                mockMvc.perform(get("/api/interventions/{numero}", "INT-2026-999999")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isNotFound());
        }

        @Test
        void deleteShouldSoftDeleteAndBeIdempotentFromTerminalStatus() throws Exception {
                String token = loginAndGetToken("user1", "pass123");
                Vehicule vehicule = activeVehicule();
                Intervention intervention = interventionRepository.save(Intervention.builder()
                                .numero("INT-2026-000002")
                                .vehicule(vehicule)
                                .type(TypeIntervention.REVISION)
                                .descriptionClient("Révision annuelle")
                                .statut(StatutIntervention.RESTITUEE)
                                .priorite(PrioriteIntervention.NORMALE)
                                .dateDepot(LocalDateTime.now())
                                .actif(true)
                                .build());

                mockMvc.perform(delete("/api/interventions/{numero}", intervention.getNumero())
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isNoContent());

                // idempotent second call
                mockMvc.perform(delete("/api/interventions/{numero}", intervention.getNumero())
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isNoContent());

                Intervention reloaded = interventionRepository.findByNumero("INT-2026-000002").orElseThrow();
                org.junit.jupiter.api.Assertions.assertFalse(reloaded.isActif());

                // no longer appears in the active listing
                mockMvc.perform(get("/api/interventions")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[?(@.numero=='INT-2026-000002')]").isEmpty());
        }

        @Test
        void deleteShouldRejectWhenStatusNotTerminal() throws Exception {
                String token = loginAndGetToken("user1", "pass123");
                Vehicule vehicule = activeVehicule();
                Intervention intervention = interventionRepository.save(Intervention.builder()
                                .numero("INT-2026-000020")
                                .vehicule(vehicule)
                                .type(TypeIntervention.REVISION)
                                .descriptionClient("Révision annuelle")
                                .statut(StatutIntervention.TERMINEE)
                                .priorite(PrioriteIntervention.NORMALE)
                                .dateDepot(LocalDateTime.now())
                                .actif(true)
                                .build());

                mockMvc.perform(delete("/api/interventions/{numero}", intervention.getNumero())
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isConflict());
        }

        @Test
        void workflowAnnulationShouldRequireManagerAndMotif() throws Exception {
                String userToken = loginAndGetToken("user1", "pass123");
                String managerToken = loginAndGetToken("manager1", "pass123");
                Vehicule vehicule = activeVehicule();

                String createResponse = mockMvc.perform(post("/api/interventions")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPayload(vehicule.getId())))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                String numero = objectMapper.readTree(createResponse).get("numero").asText();

                mockMvc.perform(post("/api/interventions/{numero}/transitions", numero)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"statutCible\":\"ANNULEE\",\"motifAnnulation\":\"Demande client\"}"))
                                .andExpect(status().isForbidden());

                mockMvc.perform(post("/api/interventions/{numero}/transitions", numero)
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"statutCible\":\"ANNULEE\",\"motifAnnulation\":\"   \"}"))
                                .andExpect(status().isUnprocessableEntity());

                mockMvc.perform(post("/api/interventions/{numero}/transitions", numero)
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"statutCible\":\"ANNULEE\",\"motifAnnulation\":\"Demande client\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statut").value("ANNULEE"));
        }

        @Test
        void updateOnInactiveInterventionShouldReturn409() throws Exception {
                String token = loginAndGetToken("user1", "pass123");
                Vehicule vehicule = vehiculeRepository.save(Vehicule.builder()
                                .immatriculationFictive("CD-"
                                                + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase()
                                                + "-EF")
                                .marque("Renault")
                                .modele("Clio")
                                .annee(2020)
                                .kilometrage(50000L)
                                .clientFictif("Client Test")
                                .actif(true)
                                .build());
                Intervention intervention = interventionRepository.save(Intervention.builder()
                                .numero("INT-2026-000003")
                                .vehicule(vehicule)
                                .type(TypeIntervention.CONTROLE)
                                .descriptionClient("Contrôle technique")
                                .statut(StatutIntervention.RECUE)
                                .priorite(PrioriteIntervention.NORMALE)
                                .dateDepot(LocalDateTime.now())
                                .actif(false)
                                .build());

                String payload = "{\"type\":\"CONTROLE\",\"descriptionClient\":\"MAJ\",\"priorite\":\"NORMALE\",\"dateDepot\":\"2026-01-01T10:00:00\"}";

                mockMvc.perform(put("/api/interventions/{numero}", intervention.getNumero())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isConflict());
        }

        @Test
        void workflowTransitionsShouldEnforceRulesAndCreateHistorique() throws Exception {
                String userToken = loginAndGetToken("user1", "pass123");
                String managerToken = loginAndGetToken("manager1", "pass123");
                Vehicule vehicule = activeVehicule();
                Mecanicien mecanicien = activeMecanicien();

                String createResponse = mockMvc.perform(post("/api/interventions")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPayload(vehicule.getId())))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                String numero = objectMapper.readTree(createResponse).get("numero").asText();

                mockMvc.perform(post("/api/interventions/{numero}/transitions", numero)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"statutCible\":\"EN_REPARATION\"}"))
                                .andExpect(status().isConflict());

                mockMvc.perform(post("/api/interventions/{numero}/transitions", numero)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"statutCible\":\"DIAGNOSTIC_EN_COURS\",\"diagnostic\":\"Plaquettes usées\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statut").value("DIAGNOSTIC_EN_COURS"));

                mockMvc.perform(post("/api/interventions/{numero}/transitions", numero)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"statutCible\":\"DEVIS_A_VALIDER\"}"))
                                .andExpect(status().isUnprocessableEntity());

                mockMvc.perform(post("/api/interventions/{numero}/transitions", numero)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"statutCible\":\"DEVIS_A_VALIDER\",\"coutEstime\":450.00,\"dateRestitutionPrevue\":\"2099-01-10T10:00:00\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statut").value("DEVIS_A_VALIDER"));

                mockMvc.perform(post("/api/interventions/{numero}/transitions", numero)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"statutCible\":\"EN_REPARATION\"}"))
                                .andExpect(status().isUnprocessableEntity());

                mockMvc.perform(patch("/api/interventions/{numero}/mecanicien", numero)
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"mecanicienId\":" + mecanicien.getId() + "}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.mecanicien.nom").value("Mec Test"));

                mockMvc.perform(post("/api/interventions/{numero}/transitions", numero)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"statutCible\":\"EN_REPARATION\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statut").value("EN_REPARATION"));

                mockMvc.perform(post("/api/interventions/{numero}/transitions", numero)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"statutCible\":\"TERMINEE\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statut").value("TERMINEE"));

                mockMvc.perform(post("/api/interventions/{numero}/transitions", numero)
                                .header("Authorization", "Bearer " + managerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"statutCible\":\"RESTITUEE\"}"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statut").value("RESTITUEE"));

                mockMvc.perform(get("/api/interventions/{numero}/historique", numero)
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalElements").value(5));
        }

        @Test
        void updateShouldReturn422WhenInterventionIsTerminal() throws Exception {
                String token = loginAndGetToken("user1", "pass123");
                Vehicule vehicule = activeVehicule();
                Intervention intervention = interventionRepository.save(Intervention.builder()
                                .numero("INT-2026-000030")
                                .vehicule(vehicule)
                                .type(TypeIntervention.REVISION)
                                .descriptionClient("Révision annuelle")
                                .statut(StatutIntervention.ANNULEE)
                                .priorite(PrioriteIntervention.NORMALE)
                                .dateDepot(LocalDateTime.now())
                                .actif(true)
                                .build());

                String payload = "{\"type\":\"REVISION\",\"descriptionClient\":\"Révision annuelle\",\"priorite\":\"NORMALE\",\"dateDepot\":\"2026-01-01T10:00:00\"}";

                mockMvc.perform(put("/api/interventions/{numero}", intervention.getNumero())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void updateShouldReturn422WhenTypeChangedAfterRecue() throws Exception {
                String token = loginAndGetToken("user1", "pass123");
                Vehicule vehicule = activeVehicule();
                LocalDateTime depot = LocalDateTime.of(2026, 1, 1, 10, 0);
                Intervention intervention = interventionRepository.save(Intervention.builder()
                                .numero("INT-2026-000031")
                                .vehicule(vehicule)
                                .type(TypeIntervention.REPARATION)
                                .descriptionClient("Bruit moteur")
                                .statut(StatutIntervention.DIAGNOSTIC_EN_COURS)
                                .priorite(PrioriteIntervention.NORMALE)
                                .dateDepot(depot)
                                .actif(true)
                                .build());

                String payload = "{\"type\":\"CONTROLE\",\"descriptionClient\":\"Bruit moteur\",\"priorite\":\"NORMALE\",\"dateDepot\":\"2026-01-01T10:00:00\"}";

                mockMvc.perform(put("/api/interventions/{numero}", intervention.getNumero())
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload))
                                .andExpect(status().isUnprocessableEntity());
        }

        @Test
        void relatedListShouldReturnOtherInterventionsSameVehiculeOnly() throws Exception {
                String userToken = loginAndGetToken("user1", "pass123");
                Vehicule vehicule = activeVehicule();

                String firstCreate = mockMvc.perform(post("/api/interventions")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPayload(vehicule.getId())))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                String firstNumero = objectMapper.readTree(firstCreate).get("numero").asText();

                String secondCreate = mockMvc.perform(post("/api/interventions")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(createPayload(vehicule.getId())))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();
                String secondNumero = objectMapper.readTree(secondCreate).get("numero").asText();

                mockMvc.perform(get("/api/interventions/{numero}/autres-interventions-vehicule", firstNumero)
                                .header("Authorization", "Bearer " + userToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.totalElements").value(1))
                                .andExpect(jsonPath("$.content[0].numero").value(secondNumero));
        }
}
