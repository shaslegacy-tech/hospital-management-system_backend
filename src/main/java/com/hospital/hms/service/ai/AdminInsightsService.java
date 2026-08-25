package com.hospital.hms.service.ai;

import com.hospital.hms.model.*;
import com.hospital.hms.model.enums.AppointmentStatus;
import com.hospital.hms.model.enums.BillStatus;
import com.hospital.hms.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminInsightsService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    private static final String GROQ_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    /**
     * Builds a real snapshot of the current hospital data.
     * No AI is involved in this method.
     */
    private String buildDataSnapshot() {

        StringBuilder sb = new StringBuilder();

        List<Doctor> doctors = doctorRepository.findAll();
        List<Appointment> appointments = appointmentRepository.findAll();
        List<Bill> bills = billRepository.findAll();

        // =========================================================
        // OVERVIEW
        // =========================================================

        sb.append("=== OVERVIEW ===\n");

        sb.append("Total doctors: ")
                .append(doctors.size())
                .append("\n");

        sb.append("Total patients: ")
                .append(patientRepository.count())
                .append("\n");

        sb.append("Total departments: ")
                .append(departmentRepository.count())
                .append("\n");

        sb.append("Total appointments (all time): ")
                .append(appointments.size())
                .append("\n\n");


        // =========================================================
        // APPOINTMENTS BY STATUS
        // =========================================================

        sb.append("=== APPOINTMENTS BY STATUS ===\n");

        Map<AppointmentStatus, Long> byStatus =
                appointments.stream()
                        .collect(
                                Collectors.groupingBy(
                                        Appointment::getStatus,
                                        Collectors.counting()
                                )
                        );

        for (AppointmentStatus status : AppointmentStatus.values()) {

            sb.append(status)
                    .append(": ")
                    .append(byStatus.getOrDefault(status, 0L))
                    .append("\n");
        }

        sb.append("\n");


        // =========================================================
        // PER DOCTOR STATS
        // =========================================================

        sb.append("=== PER-DOCTOR STATS ===\n");

        for (Doctor doctor : doctors) {

            List<Appointment> doctorAppointments =
                    appointments.stream()
                            .filter(a ->
                                    a.getDoctor() != null
                                            && a.getDoctor()
                                            .getId()
                                            .equals(doctor.getId())
                            )
                            .collect(Collectors.toList());

            long completed =
                    doctorAppointments.stream()
                            .filter(a ->
                                    a.getStatus() ==
                                            AppointmentStatus.COMPLETED
                            )
                            .count();

            long cancelled =
                    doctorAppointments.stream()
                            .filter(a ->
                                    a.getStatus() ==
                                            AppointmentStatus.CANCELLED
                            )
                            .count();

            Double avgRating =
                    reviewRepository.findAverageRatingByDoctorId(
                            doctor.getId()
                    );

            String doctorName =
                    doctor.getUser() != null
                            ? doctor.getUser().getName()
                            : "Unknown";

            String departmentName =
                    doctor.getDepartment() != null
                            ? doctor.getDepartment().getName()
                            : "Unknown";

            sb.append("- Dr. ")
                    .append(doctorName)
                    .append(" (")
                    .append(departmentName)
                    .append("): ")
                    .append(doctorAppointments.size())
                    .append(" total appointments, ")
                    .append(completed)
                    .append(" completed, ")
                    .append(cancelled)
                    .append(" cancelled, ")
                    .append("avg rating: ")
                    .append(
                            avgRating != null
                                    ? String.format("%.1f", avgRating)
                                    : "no ratings yet"
                    )
                    .append("\n");
        }

        sb.append("\n");


        // =========================================================
        // BILLING
        // =========================================================

        sb.append("=== BILLING ===\n");

        double totalRevenue =
                bills.stream()
                        .filter(b ->
                                b.getStatus() == BillStatus.PAID
                        )
                        .mapToDouble(Bill::getTotalAmount)
                        .sum();

        double pendingAmount =
                bills.stream()
                        .filter(b ->
                                b.getStatus() == BillStatus.PENDING
                        )
                        .mapToDouble(Bill::getTotalAmount)
                        .sum();

        long pendingCount =
                bills.stream()
                        .filter(b ->
                                b.getStatus() == BillStatus.PENDING
                        )
                        .count();

        sb.append("Total revenue collected (all time): ₹")
                .append(String.format("%.2f", totalRevenue))
                .append("\n");

        sb.append("Pending bills: ")
                .append(pendingCount)
                .append(" totaling ₹")
                .append(String.format("%.2f", pendingAmount))
                .append("\n\n");


        // =========================================================
        // REVENUE BY MONTH
        // =========================================================

        sb.append("=== REVENUE BY MONTH (LAST 6 MONTHS) ===\n");

        DateTimeFormatter monthFmt =
                DateTimeFormatter.ofPattern("MMM yyyy");

        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {

            LocalDate month = now.minusMonths(i);

            String label = month.format(monthFmt);

            double monthRevenue =
                    bills.stream()
                            .filter(b ->
                                    b.getStatus() == BillStatus.PAID
                            )
                            .filter(b ->
                                    b.getCreatedAt() != null
                                            && b.getCreatedAt()
                                            .getMonthValue()
                                            == month.getMonthValue()
                                            && b.getCreatedAt()
                                            .getYear()
                                            == month.getYear()
                            )
                            .mapToDouble(Bill::getTotalAmount)
                            .sum();

            sb.append(label)
                    .append(": ₹")
                    .append(String.format("%.2f", monthRevenue))
                    .append("\n");
        }

        sb.append("\n");


        // =========================================================
        // DEPARTMENTS
        // =========================================================

        sb.append("=== DEPARTMENTS ===\n");

        for (Department department :
                departmentRepository.findAll()) {

            long departmentAppointments =
                    appointments.stream()
                            .filter(a ->
                                    a.getDoctor() != null
                                            && a.getDoctor()
                                            .getDepartment() != null
                                            && a.getDoctor()
                                            .getDepartment()
                                            .getId()
                                            .equals(department.getId())
                            )
                            .count();

            sb.append("- ")
                    .append(department.getName())
                    .append(": ")
                    .append(departmentAppointments)
                    .append(" appointments\n");
        }

        return sb.toString();
    }


    /**
     * Ask AI a question about hospital data.
     */
    public String ask(String question) {
    String dataSnapshot = buildDataSnapshot();

    String systemPrompt = """
        You are a data analyst assistant for a hospital admin dashboard.

        You will be given a snapshot of real, current hospital data
        below, and an admin's question.

        STRICT RULES:
        - Answer ONLY using the data provided below.
        - NEVER invent numbers, doctors, departments, or statistics.
        - If the data doesn't contain enough information to answer,
          say so clearly rather than guessing.
        - Be concise and direct.
        - Keep the answer to 2-4 short sentences.
        - Use the exact numbers from the data when citing figures.
        - Do not explain your reasoning.
        - Return only the final answer.

        === HOSPITAL DATA SNAPSHOT ===
        %s
        """.formatted(dataSnapshot);

    JSONObject body = new JSONObject();

    body.put("model", model);

    /*
     * Qwen 3.6 supports reasoning, but Admin Insights does not need
     * deep reasoning. Disable thinking so the completion budget is
     * used for the actual answer instead of reasoning tokens.
     */
    body.put("reasoning_effort", "none");

    /*
     * max_completion_tokens includes the generated completion budget.
     * 300 is more than enough for our 2-4 sentence response.
     */
    body.put("max_completion_tokens", 300);

    body.put("temperature", 0.7);
    body.put("top_p", 0.8);

    JSONArray messages = new JSONArray();

    messages.put(
        new JSONObject()
            .put("role", "system")
            .put("content", systemPrompt)
    );

    messages.put(
        new JSONObject()
            .put("role", "user")
            .put("content", question)
    );

    body.put("messages", messages);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);

    HttpEntity<String> request =
        new HttpEntity<>(body.toString(), headers);

    RestTemplate restTemplate = new RestTemplate();

    try {
        ResponseEntity<String> response = restTemplate.exchange(
            GROQ_URL,
            HttpMethod.POST,
            request,
            String.class
        );

        JSONObject responseJson = new JSONObject(response.getBody());

        JSONObject choice =
            responseJson
                .getJSONArray("choices")
                .getJSONObject(0);

        JSONObject message = choice.getJSONObject("message");

        String content = message.optString("content", "").trim();

        /*
         * Extra protection in case Groq/model still returns an empty
         * visible response.
         */
        if (content.isEmpty()) {
            log.error(
                "Groq returned empty message content: {}",
                response.getBody()
            );

            throw new RuntimeException("AI returned an empty response");
        }

        return content;

    } catch (Exception e) {
        log.error(
            "Admin insights query failed: {}",
            e.getMessage(),
            e
        );

        throw new RuntimeException(
            "Couldn't process that question right now. Please try again."
        );
    }
}
}