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
 
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
 
    public ClinicalNotesDraftResponseDTO draft(String quickNotes) {
        String systemPrompt = """
            You are a clinical documentation assistant helping a doctor
            turn their quick shorthand notes from a patient visit into a
            properly written diagnosis and treatment record.
 
            Rules:
            - Use the doctor's notes as the ONLY source of information —
              do not invent symptoms, findings, or treatments not implied
              by the notes.
            - Write in clear, professional clinical language.
            - "diagnosis" should be a concise clinical diagnosis statement.
            - "treatment" should describe the treatment plan/recommendation
              in 1-3 sentences, based only on what the notes say.
            - This is a DRAFT for the doctor to review and edit — if the
              notes are too vague to produce a specific diagnosis, say so
              plainly in the diagnosis field rather than guessing.
 
            Respond with ONLY a JSON object, no other text, no markdown
            fences, matching exactly this shape:
            {
              "diagnosis": "...",
              "treatment": "..."
            }
            """;
 
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("max_tokens", 300);
        body.put("response_format", new JSONObject().put("type", "json_object"));
 
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
        messages.put(new JSONObject().put("role", "user").put("content", quickNotes));
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
            String text = responseJson
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content");
 
            JSONObject parsed = new JSONObject(text.trim());
 
            return new ClinicalNotesDraftResponseDTO(
                parsed.getString("diagnosis"),
                parsed.getString("treatment"),
                "AI-generated draft — please review and edit before saving."
            );
 
        } catch (Exception e) {
            log.error("AI clinical notes draft failed: {}", e.getMessage());
            throw new RuntimeException("Couldn't generate a draft right now. Please write it manually.");
        }
    }
}