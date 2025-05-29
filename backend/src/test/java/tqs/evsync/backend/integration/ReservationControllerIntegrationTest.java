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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConsumerRepository consumerRepo;

    @Autowired
    private OperatorRepository operatorRepo;

    @Autowired
    private ChargingStationRepository stationRepo;

    private Long consumerId;
    private Long stationId;

    @BeforeEach
    void setup() {
        Consumer consumer = new Consumer();
        consumer.setEmail("test@example.com");
        consumer.setPassword("1234");
        consumer.setWallet(100);
        consumer = consumerRepo.save(consumer);
        consumerId = consumer.getId();

        Operator operator = new Operator();
        operator.setEmail("op@example.com");
        operator.setPassword("1234");
        operator.setOperatorType(OperatorType.OPERATOR);
        operator = operatorRepo.save(operator);

        ChargingStation station = new ChargingStation();
        station.setLatitude(40.63);
        station.setLongitude(-8.65);
        station.setOperator(operator);
        station = stationRepo.save(station);
        stationId = station.getId();
    }

    @Test
    void testCreateReservation() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .param("consumerId", consumerId.toString())
                        .param("stationId", stationId.toString())
                        .param("startTime", "2025-06-01T09:00")
                        .param("duration", "1.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testGetReservation_NotFound() throws Exception {
        mockMvc.perform(get("/api/reservations/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testConfirmAndCancelReservation() throws Exception {
        String json = mockMvc.perform(post("/api/reservations")
                        .param("consumerId", consumerId.toString())
                        .param("stationId", stationId.toString())
                        .param("startTime", "2025-06-01T09:00")
                        .param("duration", "1.5"))
                .andReturn().getResponse().getContentAsString();

        Long reservationId = objectMapper.readTree(json).get("id").asLong();

        mockMvc.perform(post("/api/reservations/" + reservationId + "/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(post("/api/reservations/" + reservationId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
