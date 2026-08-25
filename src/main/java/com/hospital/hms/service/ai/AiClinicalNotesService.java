package com.hospital.hms.service.ai;

import com.hospital.hms.dto.response.ClinicalNotesDraftResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AiClinicalNotesService {

    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    public ClinicalNotesDraftResponseDTO draft(String quickNotes) {

        if (quickNotes == null || quickNotes.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Clinical notes cannot be empty."
            );
        }

        String systemPrompt = """
                You are a clinical documentation assistant helping a doctor
                convert quick shorthand notes from a patient visit into a
                professional clinical documentation draft.

                IMPORTANT RULES:

                1. Use ONLY the doctor's provided notes as the source of
                   information.

                2. NEVER invent symptoms, examination findings, diagnosis,
                   medications, investigations, procedures, or treatment
                   recommendations that are not present or clearly implied
                   by the doctor's notes.

                3. Do not make assumptions about missing information.

                4. Do not diagnose a condition that the doctor has not
                   documented or clearly indicated.

                5. Write in clear, professional clinical language.

                6. The "diagnosis" field should contain a concise diagnosis
                   statement based strictly on the doctor's notes.

                7. The "treatment" field should contain the treatment or
                   management plan mentioned by the doctor.

                8. If the doctor's notes are too vague to determine a
                   specific diagnosis, use exactly:
                   "Insufficient information to determine a specific diagnosis."

                9. If the treatment plan is not present in the notes, use
                   exactly:
                   "Treatment plan not specified in the provided notes."

                10. This is an AI-generated DRAFT only. The doctor must
                    review, edit, and approve it before saving it to the
                    patient's medical record.

                11. Do not provide additional medical advice beyond the
                    doctor's notes.

                12. Do not expose internal reasoning.

                Return ONLY valid JSON.

                The response MUST be a JSON object with exactly these
                two fields:

                {
                  "diagnosis": "string",
                  "treatment": "string"
                }

                Do not return markdown.
                Do not return code fences.
                Do not return any text before or after the JSON object.
                """;

        // =========================================================
        // GROQ REQUEST
        // =========================================================

        JSONObject body = new JSONObject();

        body.put("model", model);
        body.put("max_completion_tokens", 300);
        body.put("reasoning_effort", "none");

        body.put(
                "response_format",
                new JSONObject()
                        .put("type", "json_object")
        );

        body.put("temperature", 0.2);

        body.put("top_p", 0.8);

        body.put("stream", false);

        // =========================================================
        // MESSAGES
        // =========================================================

        JSONArray messages = new JSONArray();

        messages.put(
                new JSONObject()
                        .put("role", "system")
                        .put("content", systemPrompt)
        );

        messages.put(
                new JSONObject()
                        .put("role", "user")
                        .put("content", quickNotes.trim())
        );

        body.put("messages", messages);

        // =========================================================
        // HTTP REQUEST
        // =========================================================

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> request =
                new HttpEntity<>(
                        body.toString(),
                        headers
                );

        RestTemplate restTemplate = new RestTemplate();

        // =========================================================
        // CALL GROQ
        // =========================================================

        try {

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            GROQ_URL,
                            HttpMethod.POST,
                            request,
                            String.class
                    );

            if (response.getBody() == null ||
                    response.getBody().isBlank()) {

                log.error("Groq returned an empty response.");

                throw new RuntimeException(
                        "Empty response received from Groq."
                );
            }

            JSONObject responseJson =
                    new JSONObject(response.getBody());

            JSONArray choices =
                    responseJson.optJSONArray("choices");

            if (choices == null || choices.isEmpty()) {

                log.error(
                        "Groq response does not contain choices: {}",
                        response.getBody()
                );

                throw new RuntimeException(
                        "Invalid response received from Groq."
                );
            }

            JSONObject choice =
                    choices.getJSONObject(0);

            JSONObject message =
                    choice.getJSONObject("message");

            String text =
                    message.optString(
                            "content",
                            ""
                    ).trim();

            /*
             * Helpful logging when debugging Groq responses.
             */
            log.debug(
                    "Groq clinical notes response: {}",
                    response.getBody()
            );

            if (text.isBlank()) {

                String finishReason =
                        choice.optString(
                                "finish_reason",
                                "unknown"
                        );

                log.error(
                        "Groq returned empty content. finish_reason={}, response={}",
                        finishReason,
                        response.getBody()
                );

                throw new RuntimeException(
                        "AI returned an empty response."
                );
            }

            // =====================================================
            // PARSE JSON
            // =====================================================

            JSONObject parsed;

            try {

                parsed = new JSONObject(text);

            } catch (Exception jsonException) {

                log.warn(
                        "AI returned non-standard JSON. Attempting cleanup: {}",
                        text
                );

                String cleaned = text
                        .replaceAll(
                                "(?s)^```json\\s*",
                                ""
                        )
                        .replaceAll(
                                "(?s)\\s*```$",
                                ""
                        )
                        .trim();

                parsed = new JSONObject(cleaned);
            }

            String diagnosis =
                    parsed.optString(
                            "diagnosis",
                            ""
                    ).trim();

            String treatment =
                    parsed.optString(
                            "treatment",
                            ""
                    ).trim();

            if (diagnosis.isBlank()) {

                diagnosis =
                        "Insufficient information to determine a specific diagnosis.";
            }

            if (treatment.isBlank()) {

                treatment =
                        "Treatment plan not specified in the provided notes.";
            }

            return new ClinicalNotesDraftResponseDTO(
                    diagnosis,
                    treatment,
                    "AI-generated draft — please review and edit before saving."
            );

        } catch (Exception e) {

            log.error(
                    "AI clinical notes draft failed: {}",
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Couldn't generate a draft right now. Please write it manually."
            );
        }
    }
}