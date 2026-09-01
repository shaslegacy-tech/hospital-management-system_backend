package com.hospital.hms.controller;
 
import com.hospital.hms.dto.response.NearbyDoctorResponseDTO;
import com.hospital.hms.dto.response.NearbyHospitalResponseDTO;
import com.hospital.hms.model.Doctor;
import com.hospital.hms.model.Hospital;
import com.hospital.hms.model.enums.HospitalStatus;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.repository.HospitalRepository;
import com.hospital.hms.repository.ReviewRepository;
import com.hospital.hms.service.GeocodingService;
import com.hospital.hms.util.DistanceCalculator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
 
@Tag(name = "Public Discovery", description = "No-auth hospital/doctor search for patients browsing before login")
@Slf4j
@RestController
@RequestMapping("/api/public")
public class PublicDiscoveryController {
 
    @Autowired private HospitalRepository hospitalRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private GeocodingService geocodingService;
 
    @Operation(summary = "Find approved hospitals near a location, sorted by distance")
    @GetMapping("/hospitals/nearby")
    public ResponseEntity<List<NearbyHospitalResponseDTO>> nearbyHospitals(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radiusKm) {
 
        List<Hospital> candidates = hospitalRepository.findByStatus(HospitalStatus.APPROVED)
            .stream()
            .filter(h -> h.getLatitude() != null && h.getLongitude() != null)
            .collect(Collectors.toList());
 
        List<NearbyHospitalResponseDTO> result = candidates.stream()
            .map(h -> {
                double distance = DistanceCalculator.distanceKm(lat, lng, h.getLatitude(), h.getLongitude());
                return new NearbyHospitalResponseDTO(
                    h.getId(), h.getName(), h.getAddress(), h.getCity(), h.getState(),
                    h.getDescription(), h.getLogoUrl(), Math.round(distance * 10.0) / 10.0
                );
            })
            .filter(dto -> dto.getDistanceKm() <= radiusKm)
            .sorted(Comparator.comparingDouble(NearbyHospitalResponseDTO::getDistanceKm))
            .collect(Collectors.toList());
 
        log.info("Nearby hospitals search at ({}, {}) within {}km — {} results", lat, lng, radiusKm, result.size());
        return ResponseEntity.ok(result);
    }
 
    @Operation(summary = "Find available doctors near a location, sorted by their hospital's distance")
    @GetMapping("/doctors/nearby")
    public ResponseEntity<List<NearbyDoctorResponseDTO>> nearbyDoctors(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "50") double radiusKm,
            @RequestParam(required = false) String specialization) {
 
        List<Doctor> doctors = doctorRepository.findAll().stream()
            .filter(d -> d.isAvailable())
            .filter(d -> d.getDepartment().getHospital().getStatus() == HospitalStatus.APPROVED)
            .filter(d -> d.getDepartment().getHospital().getLatitude() != null
                && d.getDepartment().getHospital().getLongitude() != null)
            .filter(d -> specialization == null || specialization.isBlank()
                || d.getSpecialization().toLowerCase().contains(specialization.toLowerCase()))
            .collect(Collectors.toList());
 
        List<NearbyDoctorResponseDTO> result = doctors.stream()
            .map(d -> {
                Hospital h = d.getDepartment().getHospital();
                double distance = DistanceCalculator.distanceKm(lat, lng, h.getLatitude(), h.getLongitude());
                Double avgRating = reviewRepository.findAverageRatingByDoctorId(d.getId());
                long reviewCount = reviewRepository.countByDoctorId(d.getId());
 
                return new NearbyDoctorResponseDTO(
                    d.getId(), d.getUser().getName(), d.getSpecialization(),
                    d.getDepartment().getName(), d.getConsultationFee(),
                    avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null,
                    (int) reviewCount,
                    h.getId(), h.getName(), h.getCity(),
                    Math.round(distance * 10.0) / 10.0
                );
            })
            .filter(dto -> dto.getDistanceKm() <= radiusKm)
            .sorted(Comparator.comparingDouble(NearbyDoctorResponseDTO::getDistanceKm))
            .collect(Collectors.toList());
 
        log.info("Nearby doctors search at ({}, {}) within {}km — {} results", lat, lng, radiusKm, result.size());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Geocode a typed location (city/area) for manual search")
    @GetMapping("/geocode")
    public ResponseEntity<Map<String, Double>> geocodeLocation(
            @RequestParam String query) {
        return geocodingService.geocode(query, "", "", "")
            .map(coords -> ResponseEntity.ok(
                Map.of("lat", coords.latitude, "lng", coords.longitude)))
            .orElse(ResponseEntity.notFound().build());
    }
}