package com.hospital.hms.service.ai;

import com.hospital.hms.model.MedicalRecord;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AiVisitSummaryService {

    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";


    // =========================================================
    // GENERATE PATIENT-FRIENDLY VISIT SUMMARY
    // =========================================================

    public String summarize(MedicalRecord record) {

        // =====================================================
        // VALIDATION
        // =====================================================

        if (record == null) {
            throw new IllegalArgumentException(
                    "Medical record cannot be null."
            );
        }


        // =====================================================
        // SYSTEM PROMPT
        // =====================================================

        String systemPrompt = """
                You are a patient-friendly medical visit summary assistant.

                Your job is to explain the doctor's existing medical record
                to the patient in simple, clear, warm and reassuring language.

                The medical record is the ONLY source of information.

                STRICT RULES:

                1. Use ONLY the information provided in the medical record.

                2. NEVER invent or assume:
                   - symptoms
                   - diagnoses
                   - test results
                   - medications
                   - dosages
                   - procedures
                   - treatment plans
                   - medical conditions
                   - recovery timelines

                3. Do NOT change, reinterpret or contradict the doctor's
                   diagnosis or treatment plan.

                4. Explain medical terminology in simple everyday language
                   so that a patient without medical knowledge can understand it.

                5. Briefly explain what the documented diagnosis means,
                   based ONLY on the information provided.

                6. Briefly explain the documented treatment or management plan,
                   based ONLY on what the doctor recorded.

                7. If treatment information is not provided, do not invent
                   a treatment plan.

                8. Do NOT recommend additional medicines, tests, treatments,
                   lifestyle changes or home remedies.

                9. Do NOT provide alternative diagnoses.

                10. Do NOT make predictions about the patient's health,
                    recovery or future condition.

                11. Do NOT provide medical advice that is not explicitly
                    present in the doctor's record.

                12. Keep the response to 3-4 short sentences.

                13. Use a calm, respectful and reassuring tone.

                14. End by encouraging the patient to contact their doctor
                    if they have questions or concerns.

                15. Do NOT expose internal reasoning, chain-of-thought,
                    analysis or hidden reasoning.

                16. Return ONLY the final patient-friendly explanation.

                Do NOT return:
                - JSON
                - markdown
                - bullet points
                - headings
                - labels
                - quotation marks around the response

                The response should be plain text suitable for displaying
                directly in a patient portal.
                """;


        // =========================================================
        // BUILD MEDICAL RECORD CONTENT
        // =========================================================

        StringBuilder userContent = new StringBuilder();

        userContent
                .append("Doctor's Medical Record:\n\n")
                .append("Diagnosis: ")
                .append(
                        record.getDiagnosis() != null
                                && !record.getDiagnosis().isBlank()
                                ? record.getDiagnosis()
                                : "Not provided"
                )
                .append("\n");

        userContent
                .append("Treatment: ")
                .append(
                        record.getTreatment() != null
                                && !record.getTreatment().isBlank()
                                ? record.getTreatment()
                                : "Not provided"
                )
                .append("\n");

        if (record.getNotes() != null
                && !record.getNotes().isBlank()) {

            userContent
                    .append("Additional notes: ")
                    .append(record.getNotes())
                    .append("\n");
        }


        // =========================================================
        // GROQ REQUEST BODY
        // =========================================================

        JSONObject body = new JSONObject();

        body.put("model", model);

        /*
         * Patient summaries are short.
         *
         * 300 completion tokens is sufficient for a 3-4 sentence
         * patient-friendly explanation.
         */
        body.put("max_completion_tokens", 300);

        /*
         * IMPORTANT:
         *
         * Disable reasoning for this simple summarization task.
         *
         * Previously Qwen consumed all 600 completion tokens
         * as reasoning tokens:
         *
         * completion_tokens = 600
         * reasoning_tokens  = 600
         * content           = ""
         *
         * This caused:
         *
         * finish_reason = "length"
         *
         * and no visible response.
         */
        body.put("reasoning_effort", "none");

        /*
         * Lower temperature makes the response more consistent
         * and prevents unnecessary creativity.
         */
        body.put("temperature", 0.3);

        body.put("top_p", 0.9);

        /*
         * We are not streaming the response.
         */
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
                        .put("content", userContent.toString())
        );

        body.put("messages", messages);


        // =========================================================
        // HTTP HEADERS
        // =========================================================

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);


        // =========================================================
        // HTTP REQUEST
        // =========================================================

        HttpEntity<String> request =
                new HttpEntity<>(
                        body.toString(),
                        headers
                );

        RestTemplate restTemplate =
                new RestTemplate();


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


            // =====================================================
            // VALIDATE HTTP RESPONSE
            // =====================================================

            if (response.getBody() == null
                    || response.getBody().isBlank()) {

                log.error("Groq returned an empty HTTP response.");

                throw new RuntimeException(
                        "Empty response received from Groq."
                );
            }


            // =====================================================
            // PARSE GROQ RESPONSE
            // =====================================================

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


            // =====================================================
            // GET FIRST CHOICE
            // =====================================================

            JSONObject choice =
                    choices.getJSONObject(0);


            JSONObject message =
                    choice.optJSONObject("message");


            if (message == null) {

                log.error(
                        "Groq response does not contain message: {}",
                        response.getBody()
                );

                throw new RuntimeException(
                        "Invalid AI response received from Groq."
                );
            }


            // =====================================================
            // GET SUMMARY
            // =====================================================

            String summary =
                    message
                            .optString(
                                    "content",
                                    ""
                            )
                            .trim();


            // =====================================================
            // GET FINISH REASON
            // =====================================================

            String finishReason =
                    choice.optString(
                            "finish_reason",
                            ""
                    );


            // =====================================================
            // HANDLE EMPTY CONTENT
            // =====================================================

            if (summary.isBlank()) {

                log.error(
                        "Groq returned empty message content. " +
                        "finish_reason={}, response={}",
                        finishReason,
                        response.getBody()
                );


                if ("length".equalsIgnoreCase(finishReason)) {

                    throw new RuntimeException(
                            "AI response was cut off before generating the summary."
                    );
                }


                throw new RuntimeException(
                        "AI returned an empty response."
                );
            }


            // =====================================================
            // CLEAN UNEXPECTED MARKDOWN
            // =====================================================

            summary = summary
                    .replaceAll(
                            "^```text\\s*",
                            ""
                    )
                    .replaceAll(
                            "^```\\s*",
                            ""
                    )
                    .replaceAll(
                            "\\s*```$",
                            ""
                    )
                    .trim();


            // =====================================================
            // FINAL VALIDATION
            // =====================================================

            if (summary.isBlank()) {

                throw new RuntimeException(
                        "AI generated an empty summary."
                );
            }


            log.debug(
                    "AI visit summary generated successfully"
            );


            return summary;


        } catch (Exception e) {

            log.error(
                    "AI visit summary failed: {}",
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(
                    "Couldn't generate a summary right now. Please try again."
            );
        }
    }
}