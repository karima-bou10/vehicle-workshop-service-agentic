package com.workshop.vehicle_service.intervention.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workshop.vehicle_service.intervention.dto.AiDiagnosticPropositionResponse;
import com.workshop.vehicle_service.intervention.enums.PrioriteIntervention;
import com.workshop.vehicle_service.intervention.service.AiDiagnosticService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiDiagnosticServiceImpl implements AiDiagnosticService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiDiagnosticServiceImpl.class);

    private static final String PROMPT_TEMPLATE = """
            Tu es un assistant atelier automobile.
            A partir de la description client ci-dessous, produis une reformulation professionnelle,
            trois hypotheses de diagnostic, trois points de controle et une priorite suggeree.
            Reponds UNIQUEMENT en JSON avec les cles :
            reformulation, hypotheses (tableau), pointsControle (tableau), prioriteSuggeree (BASSE|NORMALE|HAUTE|URGENTE).

            Description client:
            %s
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final long timeoutMs;

    public AiDiagnosticServiceImpl(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper,
            @Value("${app.ai.diagnostic.timeout-ms:8000}") long timeoutMs) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public AiDiagnosticPropositionResponse generateProposition(String descriptionClient) {
        String prompt = PROMPT_TEMPLATE.formatted(descriptionClient);

        try {
            String rawResponse = CompletableFuture
                    .supplyAsync(() -> chatClient.prompt().user(prompt).call().content())
                    .get(timeoutMs, TimeUnit.MILLISECONDS);

            return parseOrFallback(rawResponse);
        } catch (TimeoutException ex) {
            LOGGER.warn("AI diagnostic provider timeout after {} ms", timeoutMs);
            return fallback();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.warn("AI diagnostic provider call interrupted");
            return fallback();
        } catch (ExecutionException ex) {
            LOGGER.warn("AI diagnostic provider unavailable: {}", ex.getClass().getSimpleName());
            return fallback();
        } catch (RuntimeException ex) {
            LOGGER.warn("AI diagnostic provider runtime failure: {}", ex.getClass().getSimpleName());
            return fallback();
        }
    }

    private AiDiagnosticPropositionResponse parseOrFallback(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return fallback();
        }

        String normalizedJson = sanitizeJson(rawResponse);
        try {
            JsonNode root = objectMapper.readTree(normalizedJson);
            if (!root.isObject()) {
                return fallback();
            }

            String reformulation = textValue(root, "reformulation");
            List<String> hypotheses = textArray(root, "hypotheses");
            List<String> pointsControle = textArray(root, "pointsControle");
            PrioriteIntervention prioriteSuggeree = priorityValue(root, "prioriteSuggeree");

            if (reformulation == null
                    || reformulation.isBlank()
                    || hypotheses.size() != 3
                    || pointsControle.size() != 3
                    || prioriteSuggeree == null) {
                return fallback();
            }

            return new AiDiagnosticPropositionResponse(
                    reformulation,
                    hypotheses,
                    pointsControle,
                    prioriteSuggeree);
        } catch (Exception ex) {
            LOGGER.warn("AI diagnostic response JSON invalid");
            return fallback();
        }
    }

    private String sanitizeJson(String rawResponse) {
        String trimmed = rawResponse.trim();
        if (trimmed.startsWith("```") && trimmed.endsWith("```")) {
            int firstNewLine = trimmed.indexOf('\n');
            if (firstNewLine > -1) {
                trimmed = trimmed.substring(firstNewLine + 1, trimmed.length() - 3).trim();
            }
        }
        return trimmed;
    }

    private String textValue(JsonNode root, String field) {
        JsonNode node = root.path(field);
        if (!node.isTextual()) {
            return null;
        }
        String value = node.asText().trim();
        return value.isEmpty() ? null : value;
    }

    private List<String> textArray(JsonNode root, String field) {
        JsonNode node = root.path(field);
        if (!node.isArray()) {
            return List.of();
        }
        return toSanitizedStringList(node);
    }

    private List<String> toSanitizedStringList(JsonNode arrayNode) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        for (JsonNode item : arrayNode) {
            if (!item.isTextual()) {
                return List.of();
            }
            String value = item.asText().trim();
            if (value.isEmpty()) {
                return List.of();
            }
            values.add(value);
        }
        return values;
    }

    private PrioriteIntervention priorityValue(JsonNode root, String field) {
        String value = textValue(root, field);
        if (value == null) {
            return null;
        }
        try {
            return PrioriteIntervention.valueOf(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private AiDiagnosticPropositionResponse fallback() {
        return new AiDiagnosticPropositionResponse(
                "Assistant indisponible, reessayez plus tard.",
                List.of(
                        "Verifier les symptomes decrits avec le client.",
                        "Effectuer un controle visuel des organes lies au symptome.",
                        "Planifier un controle complementaire si necessaire."),
                List.of(
                        "Controler visuellement les elements mecaniques concernes.",
                        "Verifier les niveaux et alertes de base du vehicule.",
                        "Documenter les observations avant validation metier."),
                PrioriteIntervention.NORMALE);
    }
}
