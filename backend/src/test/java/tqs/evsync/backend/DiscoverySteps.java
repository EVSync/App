package tqs.evsync.backend;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import tqs.evsync.backend.model.ChargingStation;
import tqs.evsync.backend.model.enums.ChargingStationStatus;
import tqs.evsync.backend.service.ChargingStationService;
import java.util.List;

public class DiscoverySteps {

    @Autowired
    private ChargingStationService chargingStationService;
    
    private List<ChargingStation> foundStations;
    private Exception searchError;

    @Given("the user Joana is logged in")
    public void user_is_logged_in() {
        // Authentication setup would go here
        // For now just ensure the service is available
        assertNotNull(chargingStationService);
    }

    @Given("location services are enabled")
    public void location_services_are_enabled() {
        // Mock location services if needed
    }

    @When("Joana searches for charging stations near latitude {string} and longitude {string} within {string} km")
    public void search_near_location(String lat, String lon, String distance) {
        try {
            foundStations = chargingStationService.getStationsNear(
                Double.parseDouble(lat),
                Double.parseDouble(lon),
                Double.parseDouble(distance)
            );
        } catch (Exception e) {
            searchError = e;
        }
    }

    @Then("she should see a list of available stations")
    public void verify_station_list() {
        assertNull(searchError, "Search failed with exception: " + searchError);
        assertNotNull(foundStations);
        assertFalse(foundStations.isEmpty());
    }

    @Then("the list should contain station {string} at {string}")
    public void verify_station_in_list(Long id, String coordinates) {
        String[] coords = coordinates.split(",");
        double lat = Double.parseDouble(coords[0]);
        double lon = Double.parseDouble(coords[1]);
        
        boolean found = foundStations.stream()
            .anyMatch(s -> 
                s.getId().equals(id) && 
                s.getLatitude() == lat && 
                s.getLongitude() == lon);
        
        assertTrue(found, "Station " + id + " not found at " + coordinates);
    }

    @Then("the list should not contain any occupied stations")
    public void verify_no_occupied_stations() {
        boolean anyOccupied = foundStations.stream()
            .anyMatch(s -> s.getStatus() != ChargingStationStatus.AVAILABLE);
        
        assertFalse(anyOccupied, "Found occupied stations in results");
    }
}