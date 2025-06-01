package tqs.evsync.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import tqs.evsync.backend.service.OpenStreetMapService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenStreetMapServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @InjectMocks
    private OpenStreetMapService osmService;

    private final String testAddress = "Lisbon, Portugal";
    private final double testLat = 38.736946;
    private final double testLon = -9.142685;

    private OpenStreetMapService.GeocodingResponse geocodingResponse;
    private OpenStreetMapService.ReverseGeocodingResponse reverseGeocodingResponse;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        
        osmService = new OpenStreetMapService(restTemplateBuilder);
        
        geocodingResponse = new OpenStreetMapService.GeocodingResponse();
        geocodingResponse.setLat("38.736946");
        geocodingResponse.setLon("-9.142685");

        reverseGeocodingResponse = new OpenStreetMapService.ReverseGeocodingResponse();
        reverseGeocodingResponse.setDisplayName("Lisbon, Portugal");
    }

    @Test
    void geocode_ValidAddress_ReturnsCoordinates() {
        when(restTemplate.getForEntity(anyString(), eq(OpenStreetMapService.GeocodingResponse[].class)))
            .thenReturn(new ResponseEntity<>(new OpenStreetMapService.GeocodingResponse[]{geocodingResponse}, HttpStatus.OK));

        OpenStreetMapService.Coordinates coords = osmService.geocode(testAddress);

        assertThat(coords.lat()).isEqualTo(testLat);
        assertThat(coords.lon()).isEqualTo(testLon);
    }

    @Test
    void geocode_EmptyResponse_ThrowsException() {
        when(restTemplate.getForEntity(anyString(), eq(OpenStreetMapService.GeocodingResponse[].class)))
            .thenReturn(new ResponseEntity<>(new OpenStreetMapService.GeocodingResponse[0], HttpStatus.OK));

        assertThrows(RuntimeException.class, () -> osmService.geocode(testAddress));
    }

    @Test
    void geocode_NullResponse_ThrowsException() {
        when(restTemplate.getForEntity(anyString(), eq(OpenStreetMapService.GeocodingResponse[].class)))
            .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThrows(RuntimeException.class, () -> osmService.geocode(testAddress));
    }

    // --- reverseGeocode() Tests ---
    @Test
    void reverseGeocode_ValidCoordinates_ReturnsAddress() {
        when(restTemplate.getForEntity(anyString(), eq(OpenStreetMapService.ReverseGeocodingResponse.class)))
            .thenReturn(new ResponseEntity<>(reverseGeocodingResponse, HttpStatus.OK));

        String address = osmService.reverseGeocode(testLat, testLon);

        assertThat(address).isEqualTo("Lisbon, Portugal");
    }

    @Test
    void reverseGeocode_NullResponse_ThrowsException() {
        when(restTemplate.getForEntity(anyString(), eq(OpenStreetMapService.ReverseGeocodingResponse.class)))
            .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        assertThrows(RuntimeException.class, () -> osmService.reverseGeocode(testLat, testLon));
    }

    @Test
    void encodeUrl_Spaces_ReplacesWithPlus() {
        String encoded = osmService.encodeUrl("A B C");
        assertThat(encoded).isEqualTo("A+B+C");
    }

    @Test
    void encodeUrl_SpecialChars_EncodesCorrectly() {
        String encoded = osmService.encodeUrl("Lisbon, Portugal");
        assertThat(encoded).isEqualTo("Lisbon%2C+Portugal");
    }
}