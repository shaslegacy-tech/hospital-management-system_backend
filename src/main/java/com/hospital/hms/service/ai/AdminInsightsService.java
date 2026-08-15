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
 
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private BillRepository billRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private ReviewRepository reviewRepository;
 
    @Value("${groq.api-key}")
    private String apiKey;
 
    @Value("${groq.model}")
    private String model;
 
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
 
    // Builds a real, aggregated snapshot of hospital data — no AI involved
    // in this part, just plain repository queries and grouping.
    private String buildDataSnapshot() {
        StringBuilder sb = new StringBuilder();
 
        List<Doctor> doctors = doctorRepository.findAll();
        List<Appointment> appointments = appointmentRepository.findAll();
        List<Bill> bills = billRepository.findAll();
 
        sb.append("=== OVERVIEW ===\n");
        sb.append("Total doctors: ").append(doctors.size()).append("\n");
        sb.append("Total patients: ").append(patientRepository.count()).append("\n");
        sb.append("Total departments: ").append(departmentRepository.count()).append("\n");
        sb.append("Total appointments (all time): ").append(appointments.size()).append("\n\n");
 
        sb.append("=== APPOINTMENTS BY STATUS ===\n");
        Map<AppointmentStatus, Long> byStatus = appointments.stream()
            .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));
        for (AppointmentStatus status : AppointmentStatus.values()) {
            sb.append(status).append(": ").append(byStatus.getOrDefault(status, 0L)).append("\n");
        }
        sb.append("\n");
 
        sb.append("=== PER-DOCTOR STATS ===\n");
        for (Doctor doctor : doctors) {
            List<Appointment> docAppointments = appointments.stream()
                .filter(a -> a.getDoctor().getId().equals(doctor.getId()))
                .collect(Collectors.toList());
            long completed = docAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
            long cancelled = docAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
            Double avgRating = reviewRepository.findAverageRatingByDoctorId(doctor.getId());
 
            sb.append("- Dr. ").append(doctor.getUser().getName())
              .append(" (").append(doctor.getDepartment().getName()).append("): ")
              .append(docAppointments.size()).append(" total appointments, ")
              .append(completed).append(" completed, ")
              .append(cancelled).append(" cancelled, ")
              .append("avg rating: ").append(avgRating != null ? String.format("%.1f", avgRating) : "no ratings yet")
              .append("\n");
        }
        sb.append("\n");
 
        sb.append("=== BILLING ===\n");
        double totalRevenue = bills.stream()
            .filter(b -> b.getStatus() == BillStatus.PAID)
            .mapToDouble(Bill::getTotalAmount).sum();
        double pendingAmount = bills.stream()
            .filter(b -> b.getStatus() == BillStatus.PENDING)
            .mapToDouble(Bill::getTotalAmount).sum();
        long pendingCount = bills.stream().filter(b -> b.getStatus() == BillStatus.PENDING).count();
        sb.append("Total revenue collected (all time): ₹").append(totalRevenue).append("\n");
        sb.append("Pending bills: ").append(pendingCount).append(" totaling ₹").append(pendingAmount).append("\n\n");
 
        sb.append("=== REVENUE BY MONTH (last 6 months) ===\n");
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yyyy");
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            String label = month.format(monthFmt);
            double monthRevenue = bills.stream()
                .filter(b -> b.getStatus() == BillStatus.PAID)
                .filter(b -> b.getCreatedAt() != null
                    && b.getCreatedAt().getMonthValue() == month.getMonthValue()
                    && b.getCreatedAt().getYear() == month.getYear())
                .mapToDouble(Bill::getTotalAmount).sum();
            sb.append(label).append(": ₹").append(monthRevenue).append("\n");
        }
        sb.append("\n");
 
        sb.append("=== DEPARTMENTS ===\n");
        for (Department dept : departmentRepository.findAll()) {
            long deptAppointments = appointments.stream()
                .filter(a -> a.getDoctor().getDepartment().getId().equals(dept.getId()))
                .count();
            sb.append("- ").append(dept.getName()).append(": ")
              .append(deptAppointments).append(" appointments\n");
        }
 
        return sb.toString();
    }
 
    public String ask(String question) {
        String dataSnapshot = buildDataSnapshot();
 
        String systemPrompt = """
            You are a data analyst assistant for a hospital admin dashboard.
            You will be given a snapshot of real, current hospital data
            below, and an admin's question.
 
            STRICT RULES:
            - Answer ONLY using the data provided below.
            - NEVER invent numbers, doctors, or statistics not present here.
            - If the data doesn't contain enough information to answer,
              say so clearly rather than guessing.
            - Be concise — 2-4 sentences, lead with the direct answer.
            - Use the exact numbers from the data when citing figures.
 
            === HOSPITAL DATA SNAPSHOT ===
            %s
            """.formatted(dataSnapshot);
 
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("max_tokens", 400);
 
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
        messages.put(new JSONObject().put("role", "user").put("content", question));
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
            log.error("Admin insights query failed: {}", e.getMessage());
            throw new RuntimeException("Couldn't process that question right now. Please try again.");
        }
    }
}
