package com.hospital.hms.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Slf4j
@Service
public class GeocodingService {

    private static final String NOMINATIM_URL =
            "https://nominatim.openstreetmap.org/search";

    public static class Coordinates {

        public final double latitude;
        public final double longitude;

        public Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public Optional<Coordinates> geocode(
            String address,
            String city,
            String state,
            String pincode) {

        try {

            String query = String.format(
                    "%s, %s, %s, %s, India",
                    address,
                    city,
                    state,
                    pincode
            );

            String url = UriComponentsBuilder
                    .fromUriString(NOMINATIM_URL)
                    .queryParam("q", query)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build()
                    .encode()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();

            // Nominatim requires a User-Agent
            headers.set(
                    "User-Agent",
                    "MedCareHMS/1.0 (contact@medcare.example.com)"
            );

            HttpEntity<Void> request =
                    new HttpEntity<>(headers);

            RestTemplate restTemplate =
                    new RestTemplate();

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            request,
                            String.class
                    );

            if (response.getBody() == null
                    || response.getBody().isBlank()) {

                log.warn(
                        "Empty geocoding response for address: {}",
                        query
                );

                return Optional.empty();
            }

            JSONArray results =
                    new JSONArray(response.getBody());

            if (results.isEmpty()) {

                log.warn(
                        "No geocoding results for address: {}",
                        query
                );

                return Optional.empty();
            }

            JSONObject first =
                    results.getJSONObject(0);

            double lat =
                    Double.parseDouble(
                            first.getString("lat")
                    );

            double lon =
                    Double.parseDouble(
                            first.getString("lon")
                    );

            log.info(
                    "Geocoded '{}' -> ({}, {})",
                    query,
                    lat,
                    lon
            );

            return Optional.of(
                    new Coordinates(lat, lon)
            );

        } catch (Exception e) {

            // Geocoding failure should not block
            // hospital registration.

            log.error(
                    "Geocoding failed for address: {}",
                    e.getMessage(),
                    e
            );

            return Optional.empty();
        }
    }
}