package com.hospital.hms.service.ai;

import com.hospital.hms.dto.response.SymptomCheckResponseDTO;
import com.hospital.hms.model.Department;
import com.hospital.hms.repository.DepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiSymptomCheckerService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    public SymptomCheckResponseDTO checkSymptoms(String symptoms) {

        List<Department> departments =
                departmentRepository.findAll();

        String departmentList = departments.stream()
                .map(d -> "- " + d.getName() + ": " + d.getDescription())
                .collect(Collectors.joining("\n"));

        String systemPrompt = """
                You are a triage assistant for a hospital booking app.

                Your ONLY job is to suggest the hospital department
                that the patient should book based on the symptoms.

                NEVER diagnose a disease.
                NEVER prescribe medicine.
                NEVER provide treatment instructions.

                Available hospital departments:

                %s

                You MUST select departmentName EXACTLY from the
                department list above.

                If the symptoms could indicate a medical emergency,
                such as:
                - chest pain
                - severe difficulty breathing
                - severe bleeding
                - stroke symptoms
                - loss of consciousness
                - severe allergic reaction

                then set urgencyLevel to HIGH.

                For HIGH urgency, clearly tell the patient to seek
                emergency medical care immediately instead of waiting
                for a routine appointment.

                Return ONLY valid JSON.

                Required JSON structure:

                {
                  "departmentName": "exact department name",
                  "explanation": "1-2 short plain-language sentences",
                  "urgencyLevel": "LOW"
                }

                urgencyLevel must be exactly one of:
                LOW, MEDIUM, HIGH.
                """.formatted(departmentList);

        JSONObject body = new JSONObject();

        body.put("model", model);

        body.put("messages", new JSONArray()
                .put(new JSONObject()
                        .put("role", "system")
                        .put("content", systemPrompt))
                .put(new JSONObject()
                        .put("role", "user")
                        .put("content", symptoms))
        );

        // IMPORTANT
        body.put(
                "response_format",
                new JSONObject()
                        .put("type", "json_object")
        );

        // Qwen 3.6 supports disabling reasoning
        body.put("reasoning_effort", "none");

        body.put("temperature", 0.2);

        body.put("max_completion_tokens", 500);

        body.put("stream", false);

        log.info("Calling Groq model: {}", model);

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> request =
                new HttpEntity<>(body.toString(), headers);

        RestTemplate restTemplate = new RestTemplate();

        try {

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            GROQ_URL,
                            HttpMethod.POST,
                            request,
                            String.class
                    );

            log.info(
                    "Groq HTTP status: {}",
                    response.getStatusCode()
            );

            log.debug(
                    "Groq raw response: {}",
                    response.getBody()
            );

            if (response.getBody() == null ||
                    response.getBody().isBlank()) {

                throw new RuntimeException(
                        "Groq returned an empty response."
                );
            }

            JSONObject responseJson =
                    new JSONObject(response.getBody());

            JSONArray choices =
                    responseJson.getJSONArray("choices");

            if (choices.isEmpty()) {
                throw new RuntimeException(
                        "Groq returned no choices."
                );
            }

            JSONObject message =
                    choices
                            .getJSONObject(0)
                            .getJSONObject("message");

            String rawText =
                    message.optString("content", "");

            log.info(
                    "Groq AI content: {}",
                    rawText
            );

            if (rawText.isBlank()) {

                throw new RuntimeException(
                        "Groq returned empty message content."
                );
            }

            JSONObject parsed =
                    new JSONObject(rawText);

            String departmentName =
                    parsed.optString(
                            "departmentName",
                            ""
                    );

            String explanation =
                    parsed.optString(
                            "explanation",
                            ""
                    );

            String urgencyLevel =
                    parsed.optString(
                            "urgencyLevel",
                            "LOW"
                    ).toUpperCase();

            if (departmentName.isBlank()) {
                throw new RuntimeException(
                        "AI did not return departmentName."
                );
            }

            if (explanation.isBlank()) {
                explanation =
                        "Based on the symptoms, this department "
                        + "may be appropriate for further evaluation.";
            }

            if (!urgencyLevel.equals("LOW")
                    && !urgencyLevel.equals("MEDIUM")
                    && !urgencyLevel.equals("HIGH")) {

                urgencyLevel = "LOW";
            }

            /*
             * Match AI department with actual DB department.
             */
            Department matched =
                    departments.stream()
                            .filter(d ->
                                    d.getName()
                                            .equalsIgnoreCase(
                                                    departmentName
                                            )
                            )
                            .findFirst()
                            .orElse(null);

            /*
             * Do NOT silently pick the first department.
             * If AI returns an invalid department, fail safely.
             */
            if (matched == null) {

                log.warn(
                        "AI returned unknown department: {}",
                        departmentName
                );

                throw new RuntimeException(
                        "AI suggested an unavailable hospital department."
                );
            }

            return new SymptomCheckResponseDTO(

                    matched.getId(),

                    matched.getName(),

                    explanation,

                    urgencyLevel,

                    "This is general guidance, not a medical diagnosis. "
                            + "If this is an emergency, call your local "
                            + "emergency number immediately."
            );

        } catch (Exception e) {

            log.error(
                    "AI symptom check failed",
                    e
            );

            throw new RuntimeException(
                    "Couldn't process your symptoms right now. "
                            + "Please try again or search doctors directly."
            );
        }
    }
}