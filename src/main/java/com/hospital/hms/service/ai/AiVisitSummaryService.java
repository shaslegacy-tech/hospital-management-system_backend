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
 
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
 
    public String summarize(MedicalRecord record) {
        String systemPrompt = """
            You are a friendly assistant that explains a doctor's medical
            notes to a patient in plain, warm, reassuring language. The
            patient has no medical background.
 
            Rules:
            - Explain what the diagnosis means in everyday words, no jargon.
            - Briefly explain what the treatment involves and why.
            - Do NOT add any new medical advice beyond what's in the notes.
            - Do NOT suggest medications, dosages, or alternative treatments.
            - Keep it to 3-4 short sentences.
            - End by encouraging them to contact their doctor with any questions.
            - Respond with ONLY the explanation text, no headers, no JSON, no quotes.
            """;
 
        String userContent = "Diagnosis: " + record.getDiagnosis() +
            "\nTreatment: " + record.getTreatment() +
            (record.getNotes() != null ? "\nAdditional notes: " + record.getNotes() : "");
 
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("max_tokens", 250);
 
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
        messages.put(new JSONObject().put("role", "user").put("content", userContent));
        body.put("messages", messages);
 
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
 
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
 
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                GROQ_URL, HttpMethod.POST, request, String.class);
 
            JSONObject responseJson = new JSONObject(response.getBody());
            return responseJson
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim();
 
        } catch (Exception e) {
            log.error("AI visit summary failed: {}", e.getMessage());
            throw new RuntimeException("Couldn't generate a summary right now. Please try again.");
        }
    }
}