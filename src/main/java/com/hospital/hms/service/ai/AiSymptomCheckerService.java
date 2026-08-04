package com.hospital.hms.service.ai;

import com.hospital.hms.dto.response.SymptomCheckResponseDTO;
import com.hospital.hms.model.Department;
import com.hospital.hms.repository.DepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    // Groq's API is OpenAI-compatible — same request/response shape as
    // OpenAI's chat completions endpoint, just a different base URL.
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    public SymptomCheckResponseDTO checkSymptoms(String symptoms) {
        List<Department> departments = departmentRepository.findAll();

        String departmentList = departments.stream()
                .map(d -> "- " + d.getName() + ": " + d.getDescription())
                .collect(Collectors.joining("\n"));

        String systemPrompt = """
            You are a triage assistant for a hospital booking app. A patient
            will describe their symptoms in plain language. Your job is ONLY
            to suggest which hospital department they should book with —
            you must NEVER diagnose a condition, suggest medication, or give
            treatment advice.
 
            You MUST pick departmentName EXACTLY from this list of real
            departments this hospital has (do not invent one):
            %s
 
            If the symptoms described could indicate a medical emergency
            (e.g. chest pain, difficulty breathing, severe bleeding, signs
            of stroke, loss of consciousness, severe allergic reaction),
            set urgencyLevel to "HIGH" and the explanation must clearly tell
            the person to seek emergency care immediately rather than book
            a routine appointment.
 
            Respond with ONLY a JSON object, no other text, no markdown
            fences, matching exactly this shape:
            {
              "departmentName": "<exact name from the list above>",
              "explanation": "<1-2 short sentences, plain language, no jargon>",
              "urgencyLevel": "LOW" | "MEDIUM" | "HIGH"
            }
            """.formatted(departmentList);

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("max_tokens", 300);
        body.put("response_format", new JSONObject().put("type", "json_object"));

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
        messages.put(new JSONObject().put("role", "user").put("content", symptoms));
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
            String departmentName = parsed.getString("departmentName");
            String explanation = parsed.getString("explanation");
            String urgencyLevel = parsed.getString("urgencyLevel");

            Department matched = departments.stream()
                    .filter(d -> d.getName().equalsIgnoreCase(departmentName))
                    .findFirst()
                    .orElse(departments.isEmpty() ? null : departments.get(0));

            return new SymptomCheckResponseDTO(
                    matched != null ? matched.getId() : null,
                    matched != null ? matched.getName() : departmentName,
                    explanation,
                    urgencyLevel,
                    "This is general guidance, not a medical diagnosis. If this " +
                            "is an emergency, call your local emergency number immediately."
            );

        } catch (Exception e) {
            log.error("AI symptom check failed: {}", e.getMessage());
            throw new RuntimeException(
                    "Couldn't process your symptoms right now. Please try again or search doctors directly.");
        }
    }
}
