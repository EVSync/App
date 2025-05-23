package tqs.evsync.backend.service;

import java.nio.charset.StandardCharsets;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;

import lombok.Getter;
import lombok.Setter;

@Service
public class OpenStreetMapService {
    private static final String OSM_NOMINATIM_API = "https://nominatim.openstreetmap.org";

    private final RestTemplate restTemplate;

    public OpenStreetMapService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    // Convert address to coordinates (latitude, longitude)
    public Coordinates geocode(String address) {
        String url = String.format("%s/search?q=%s&format=json", OSM_NOMINATIM_API, encodeUrl(address));
        ResponseEntity<GeocodingResponse[]> response = restTemplate.getForEntity(url, GeocodingResponse[].class);
        
        if (response.getBody() == null || response.getBody().length == 0) {
            throw new RuntimeException("No results found for address: " + address);
        }
        GeocodingResponse firstResult = response.getBody()[0];
        return new Coordinates(Double.parseDouble(firstResult.getLat()), Double.parseDouble(firstResult.getLon()));
    }

    // Convert coordinates to address
    public String reverseGeocode(double lat, double lon) {
        String url = String.format("%s/reverse?lat=%f&lon=%f&format=json", OSM_NOMINATIM_API, lat, lon);
        ResponseEntity<ReverseGeocodingResponse> response = restTemplate.getForEntity(url, ReverseGeocodingResponse.class);
        
        if (response.getBody() == null) {
            throw new RuntimeException("No address found for coordinates: " + lat + ", " + lon);
        }
        return response.getBody().getDisplayName();
    }

    // Encode URL parameters
    public String encodeUrl(String input) {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    // DTOs for OSM API responses
    @Getter @Setter
    public static class GeocodingResponse {
        private String lat;
        private String lon;
    }

    @Getter @Setter
    public static class ReverseGeocodingResponse {
        private String displayName;
    }

    public record Coordinates(double lat, double lon) {}
}
