package tqs.evsync.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tqs.evsync.backend.model.*;
import tqs.evsync.backend.model.enums.OperatorType;
import tqs.evsync.backend.repository.*;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SessionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChargingOutletRepository outletRepo;

    @Autowired
    private ChargingStationRepository stationRepo;

    @Autowired
    private OperatorRepository operatorRepo;

    private Long outletId;

    @BeforeEach
    void setup() {
        Operator operator = new Operator();
        operator.setEmail("op_session@example.com");
        operator.setPassword("1234");
        operator.setOperatorType(OperatorType.OPERATOR);
        operator = operatorRepo.save(operator);

        ChargingStation station = new ChargingStation();
        station.setLatitude(40.63);
        station.setLongitude(-8.65);
        station.setOperator(operator);
        station = stationRepo.save(station);

        ChargingOutlet outlet = new ChargingOutlet();
        outlet.setMaxPower(22);
        outlet.setChargingStation(station); // associar à station
        outlet = outletRepo.save(outlet);

        outletId = outlet.getId();
    }

    @Test
    void testCreateSessionIntegration() throws Exception {
        Map<String, Object> payload = Map.of("outletId", outletId);

        mockMvc.perform(post("/api/v1/sessions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testGetAllSessions() throws Exception {
        mockMvc.perform(get("/api/v1/sessions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testGetSessionById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/sessions/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Session not found."));
    }

    @Test
    void testEndSession_InvalidId() throws Exception {
        Map<String, Object> payload = Map.of(
                "energyUsed", 12.5,
                "totalCost", 3.75
        );

        mockMvc.perform(put("/api/v1/sessions/999999/end")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteSession_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/sessions/999999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Session not found."));
    }

    @Test
    void testSessionLifecycle() throws Exception {
        String response = mockMvc.perform(post("/api/v1/sessions")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("outletId", outletId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long sessionId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/api/v1/sessions/" + sessionId + "/end")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "energyUsed", 15.0,
                                "totalCost", 7.5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.energyUsed").value(15.0))
                .andExpect(jsonPath("$.totalCost").value(7.5));
    }
}
