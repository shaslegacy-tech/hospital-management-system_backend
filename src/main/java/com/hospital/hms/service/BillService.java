package com.hospital.hms.service;

import com.hospital.hms.dto.request.*;
import com.hospital.hms.dto.response.AppointmentResponseDTO;
import com.hospital.hms.dto.response.BillResponseDTO;
import com.hospital.hms.model.*;
import com.hospital.hms.model.enums.*;
import com.hospital.hms.repository.*;
import com.hospital.hms.service.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmailService emailService;

    private BillResponseDTO toDTO(Bill bill) {
        return new BillResponseDTO(
                bill.getId(),
                bill.getAppointment().getId(),
                bill.getAppointment()
                        .getPatient().getUser().getName(),
                bill.getAppointment()
                        .getDoctor().getUser().getName(),
                bill.getAppointment()
                        .getDoctor().getDepartment().getName(),
                bill.getConsultationFee(),
                bill.getAdditionalCharges(),
                bill.getTotalAmount(),
                bill.getStatus(),
                bill.getPaymentMethod(),
                bill.getNotes(),
                bill.getCreatedAt()
        );
    }

    // GET bill by appointment
    public BillResponseDTO getBillByAppointment(
            Long appointmentId) {
        Bill bill = billRepository
                .findByAppointmentId(appointmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bill not found for appointment: "
                                        + appointmentId));
        return toDTO(bill);
    }

    // GET bill by ID
    public BillResponseDTO getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bill not found: " + id));
        return toDTO(bill);
    }

    // GET bills by patient
    public List<BillResponseDTO> getBillsByPatient(
            Long patientId) {
        return billRepository
                .findByAppointmentPatientId(patientId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // POST - generate bill
    public BillResponseDTO generateBill(
            BillRequestDTO dto) {
        log.info("Generating bill for appointment: {}",
                dto.getAppointmentId());

        Appointment appointment =
                appointmentRepository
                        .findById(dto.getAppointmentId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Appointment not found!"));

        // Only bill COMPLETED appointments
        if (appointment.getStatus() !=
                AppointmentStatus.COMPLETED) {
            throw new RuntimeException(
                    "Appointment must be COMPLETED first!");
        }

        // Check bill doesn't exist
        if (billRepository.findByAppointmentId(
                dto.getAppointmentId()).isPresent()) {
            throw new RuntimeException(
                    "Bill already generated!");
        }

        double consultationFee =
                appointment.getDoctor().getConsultationFee();
        double additional =
                dto.getAdditionalCharges() != null
                        ? dto.getAdditionalCharges() : 0.0;
        double total = consultationFee + additional;

        Bill bill = new Bill();
        bill.setAppointment(appointment);
        bill.setConsultationFee(consultationFee);
        bill.setAdditionalCharges(additional);
        bill.setTotalAmount(total);
        bill.setStatus(BillStatus.PENDING);
        bill.setNotes(dto.getNotes());

        Bill saved = billRepository.save(bill);

        // Send bill email
        emailService.sendBillGeneratedEmail(saved);

        log.info("Bill generated + email sent!");
        return toDTO(saved);
    }

    // PUT - mark as paid
    public BillResponseDTO payBill(Long id,
                                   BillPaymentDTO dto) {
        log.info("Processing payment for bill: {}", id);

        Bill bill = billRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Bill not found: " + id));

        if (bill.getStatus() == BillStatus.PAID) {
            throw new RuntimeException(
                    "Bill already paid!");
        }

        bill.setStatus(BillStatus.PAID);
        bill.setPaymentMethod(dto.getPaymentMethod());

        log.info("Payment successful via {}",
                dto.getPaymentMethod());
        return toDTO(billRepository.save(bill));
    }

    // GET total revenue
    public Double getTotalRevenue() {
        Double revenue = billRepository.getTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }

    // GET all appointments
    public Page<BillResponseDTO> getAllBills(
            int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return billRepository.findAll(pageable)
                .map(this::toDTO);
    }
}