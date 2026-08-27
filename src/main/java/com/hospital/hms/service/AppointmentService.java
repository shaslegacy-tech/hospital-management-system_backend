package com.hospital.hms.service;

import com.hospital.hms.dto.request.*;
import com.hospital.hms.dto.response.AppointmentResponseDTO;
import com.hospital.hms.model.*;
import com.hospital.hms.model.enums.AppointmentStatus;
import com.hospital.hms.repository.*;
import com.hospital.hms.repository.spec.AppointmentSpecification;
import com.hospital.hms.service.email.EmailService;
import com.hospital.hms.model.Hospital;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Autowired 
    private AuditLogService auditLogService;

    @Autowired
    private HospitalContextService hospitalContextService;

    private AppointmentResponseDTO toDTO(
            Appointment appointment) {
        return new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getUser().getName(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getUser().getName(),
                appointment.getDoctor().getDepartment().getName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getStatus(),
                appointment.getReason(),
                appointment.getNotes(),
                appointment.getAmount(),
                appointment.getCreatedAt()
        );
    }

    // GET all appointments
    public Page<AppointmentResponseDTO> getAllAppointments(
            Authentication authentication,
            int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("appointmentDate").descending());
        
        // ✅ Filter by hospital unless SUPER_ADMIN
        if (hospitalContextService.isSuperAdmin(authentication)) {
            return appointmentRepository.findAll(pageable)
                    .map(this::toDTO);
        }
        
        Hospital hospital = hospitalContextService.getCurrentUserHospital(authentication);
        Specification<Appointment> spec = (root, query, cb) ->
                cb.equal(root.get("doctor").get("department").get("hospital").get("id"),
                        hospital.getId());
        
        return appointmentRepository.findAll(spec, pageable)
                .map(this::toDTO);
    }

    // GET appointment by ID
    public AppointmentResponseDTO getAppointmentById(
            Long id, Authentication authentication) {
        Appointment appointment =
                appointmentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Appointment not found: " + id));
        
        // ✅ Validate appointment belongs to user's hospital (unless SUPER_ADMIN)
        if (!hospitalContextService.isSuperAdmin(authentication)) {
            Hospital hospital = hospitalContextService.getCurrentUserHospital(authentication);
            if (!appointment.getDoctor().getDepartment().getHospital().getId()
                    .equals(hospital.getId())) {
                throw new RuntimeException(
                        "Appointment does not belong to your hospital");
            }
        }
        
        return toDTO(appointment);
    }

    // GET appointments by patient
    public Page<AppointmentResponseDTO>
    getAppointmentsByPatient(Long patientId,
                             int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("appointmentDate").descending());
        return appointmentRepository
                .findByPatientId(patientId, pageable)
                .map(this::toDTO);
    }

    // GET appointments by doctor
    public Page<AppointmentResponseDTO>
    getAppointmentsByDoctor(Long doctorId,
                            Authentication authentication,
                            int page, int size) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found!"));
        
        // ✅ Validate doctor belongs to hospital (unless SUPER_ADMIN)
        if (!hospitalContextService.isSuperAdmin(authentication)) {
            Hospital hospital = hospitalContextService.getCurrentUserHospital(authentication);
            if (!doctor.getDepartment().getHospital().getId()
                    .equals(hospital.getId())) {
                throw new RuntimeException(
                        "Doctor does not belong to your hospital");
            }
        }
        
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("appointmentDate").descending());
        return appointmentRepository
                .findByDoctorId(doctorId, pageable)
                .map(this::toDTO);
    }

    // GET today's appointments
    public List<AppointmentResponseDTO>
    getTodaysAppointments(Authentication authentication) {
        log.info("Fetching today's appointments");
        
        List<Appointment> appointments = appointmentRepository
                .findByAppointmentDate(LocalDate.now());
        
        // ✅ Filter by hospital unless SUPER_ADMIN
        if (hospitalContextService.isSuperAdmin(authentication)) {
            return appointments.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        
        Hospital hospital = hospitalContextService.getCurrentUserHospital(authentication);
        return appointments.stream()
                .filter(a -> a.getDoctor().getDepartment().getHospital().getId()
                        .equals(hospital.getId()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // GET doctor's schedule for a date
    public List<AppointmentResponseDTO>
    getDoctorSchedule(Long doctorId,
                      LocalDate date,
                      Authentication authentication) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found!"));
        
        // ✅ Validate doctor belongs to hospital (unless SUPER_ADMIN)
        if (!hospitalContextService.isSuperAdmin(authentication)) {
            Hospital hospital = hospitalContextService.getCurrentUserHospital(authentication);
            if (!doctor.getDepartment().getHospital().getId()
                    .equals(hospital.getId())) {
                throw new RuntimeException(
                        "Doctor does not belong to your hospital");
            }
        }
        
        return appointmentRepository
                .findByDoctorIdAndAppointmentDate(
                        doctorId, date)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // POST - book appointment
    public AppointmentResponseDTO bookAppointment(
            AppointmentRequestDTO dto,
            Authentication authentication) {
        log.info("Booking appointment for patient: {}",
                dto.getPatientId());

        Patient patient = patientRepository
                .findById(dto.getPatientId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient not found!"));

        Doctor doctor = doctorRepository
                .findById(dto.getDoctorId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Doctor not found!"));

        // ✅ Validate doctor belongs to patient's hospital (unless SUPER_ADMIN)
        if (!hospitalContextService.isSuperAdmin(authentication)) {
            Hospital hospital = hospitalContextService.getCurrentUserHospital(authentication);
            if (!doctor.getDepartment().getHospital().getId()
                    .equals(hospital.getId())) {
                throw new RuntimeException(
                        "Doctor does not belong to your hospital");
            }
        }

        // Check doctor is available
        if (!doctor.isAvailable()) {
            throw new RuntimeException(
                    "Doctor is not available!");
        }

        // Check slot is not already booked
        boolean slotTaken = appointmentRepository
                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                        dto.getDoctorId(),
                        dto.getAppointmentDate(),
                        dto.getAppointmentTime(),
                        AppointmentStatus.CANCELLED);

        if (slotTaken) {
            throw new RuntimeException(
                    "This time slot is already booked!");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(
                dto.getAppointmentDate());
        appointment.setAppointmentTime(
                dto.getAppointmentTime());
        appointment.setReason(dto.getReason());
        appointment.setNotes(dto.getNotes());
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setAmount(doctor.getConsultationFee());

        Appointment saved =
                appointmentRepository.save(appointment);

          auditLogService.log(
                saved.getPatient().getUser(), "APPOINTMENT_BOOKED", "Appointment",
                saved.getId(),
                saved.getPatient().getUser().getName() + " booked with Dr. " +
                saved.getDoctor().getUser().getName() + " on " + saved.getAppointmentDate());

        // Send email notification
        emailService.sendAppointmentBookedEmail(saved);

        // Send app notification to doctor
        notificationService.notify(
                saved.getDoctor().getUser(),
                "APPOINTMENT_BOOKED",
                "New appointment booked by " +
                        saved.getPatient().getUser().getName(),
                "/doctor/schedule"
        );

        notificationService.notify(
                saved.getPatient().getUser(),
                "APPOINTMENT_BOOKED",
                "Your appointment with Dr. " + saved.getDoctor().getUser().getName() + "...",
                "/appointments"
        );
        log.info("Appointment booked + email sent + notification created!");
        return toDTO(saved);
    }

    // PUT - update appointment status
    public AppointmentResponseDTO updateStatus(
            Long id,
            AppointmentStatusUpdateDTO dto) {
        log.info("Updating appointment {} status to {}",
                id, dto.getStatus());

        Appointment appointment =
                appointmentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Appointment not found: " + id));

        appointment.setStatus(dto.getStatus());
        if (dto.getNotes() != null) {
            appointment.setNotes(dto.getNotes());
        }

        Appointment saved =
                appointmentRepository.save(appointment);

        auditLogService.log(
                saved.getPatient().getUser(),
                "APPOINTMENT_STATUS_CHANGED",
                "Appointment",
                saved.getId(),
                "Status changed to " + saved.getStatus());

        // Send email based on new status
        if (dto.getStatus() == AppointmentStatus.CONFIRMED) {
            emailService.sendAppointmentConfirmedEmail(saved);

            notificationService.notify(
                    saved.getPatient().getUser(),
                    "APPOINTMENT_CONFIRMED",
                    "Your appointment with Dr. " +
                            saved.getDoctor().getUser().getName() +
                            " was confirmed",
                    "/appointments"
            );
        } else if (dto.getStatus() ==
                AppointmentStatus.CANCELLED) {
            emailService.sendAppointmentCancelledEmail(saved);

            notificationService.notify(
                    saved.getPatient().getUser(),
                    "APPOINTMENT_CANCELLED",
                    "Your appointment on " +
                            saved.getAppointmentDate() +
                            " was cancelled",
                    "/appointments"
            );
        }

        return toDTO(saved);
    }

    // PUT - cancel appointment
    public AppointmentResponseDTO cancelAppointment(
            Long id) {
        log.info("Cancelling appointment: {}", id);

        Appointment appointment =
                appointmentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Appointment not found: " + id));

        if (appointment.getStatus() ==
                AppointmentStatus.COMPLETED) {
            throw new RuntimeException(
                    "Cannot cancel completed appointment!");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment saved =
                appointmentRepository.save(appointment);

        auditLogService.log(
                saved.getPatient().getUser(),
                "APPOINTMENT_CANCELLED",
                "Appointment",
                saved.getId(),
                "Appointment Cancelled");   

        // Send cancellation email
        emailService.sendAppointmentCancelledEmail(saved);

        // Send app notification to patient
        notificationService.notify(
                saved.getPatient().getUser(),
                "APPOINTMENT_CANCELLED",
                "Your appointment on " +
                        saved.getAppointmentDate() +
                        " was cancelled",
                "/appointments"
        );

        return toDTO(saved);
    }

    // DELETE appointment — ADMIN only
    public void deleteAppointment(Long id) {
        log.warn("Deleting appointment: {}", id);
        appointmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Appointment not found: " + id));
        appointmentRepository.deleteById(id);
    }

    public Page<AppointmentResponseDTO> searchAppointments(
            AppointmentStatus status,
            Long doctorId,
            Long patientId,
            Long departmentId,
            LocalDate dateFrom,
            LocalDate dateTo,
            Authentication authentication,
            int page, int size) {

        log.info("Searching appointments with filters");

        Specification<Appointment> spec = Specification
                .where(AppointmentSpecification.hasStatus(status))
                .and(AppointmentSpecification.hasDoctorId(
                        doctorId))
                .and(AppointmentSpecification.hasPatientId(
                        patientId))
                .and(AppointmentSpecification.hasDepartmentId(
                        departmentId))
                .and(AppointmentSpecification.dateFrom(dateFrom))
                .and(AppointmentSpecification.dateTo(dateTo));

        // ✅ Add hospital scoping (unless SUPER_ADMIN)
        if (!hospitalContextService.isSuperAdmin(authentication)) {
            Hospital hospital = hospitalContextService.getCurrentUserHospital(authentication);
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("doctor").get("department").get("hospital").get("id"),
                            hospital.getId()));
        }

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("appointmentDate").descending());

        return appointmentRepository.findAll(spec, pageable)
                .map(this::toDTO);
    }
}