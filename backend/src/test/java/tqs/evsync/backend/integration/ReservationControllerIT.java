package tqs.evsync.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;
import tqs.evsync.backend.model.*;
import tqs.evsync.backend.model.enums.OperatorType;
import tqs.evsync.backend.model.enums.ReservationStatus;
import tqs.evsync.backend.repository.*;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.profiles.active=test")
@ActiveProfiles("test")
public class ReservationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConsumerRepository consumerRepo;

    @Autowired
    private OperatorRepository operatorRepo;

    @Autowired
    private ReservationRepository reservationRepo;

    @Autowired
    private ChargingOutletRepository outletRepo;

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
        ChargingOutlet outlet = new ChargingOutlet();
        outlet.setMaxPower(22);
        outlet.setAvailable(true);
        outlet.setChargingStation(stationRepo.findById(stationId).orElseThrow());
        outletRepo.save(outlet);

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)  // Keep as form data
                .param("consumerId", consumerId.toString())
                .param("stationId", stationId.toString())
                .param("startTime", "2025-06-01T09:00:00")  // Match controller format
                .param("duration", "1.5"))
                .andDo(print())  // Add this for debugging
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
        Consumer consumer = consumerRepo.findById(consumerId).orElseThrow();
        consumer.setWallet(100.0);
        consumerRepo.save(consumer);
        
        ChargingOutlet outlet = new ChargingOutlet();
        outlet.setMaxPower(22);
        outlet.setAvailable(true);
        outlet.setChargingStation(stationRepo.findById(stationId).orElseThrow());
        outletRepo.save(outlet);

        MvcResult createResult = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("consumerId", consumerId.toString())
                        .param("stationId", stationId.toString())
                        .param("startTime", "2025-06-01T09:00:00")
                        .param("duration", "1.5"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ReservationStatus.PENDING.name()))
                .andReturn();
        
        String responseContent = createResult.getResponse().getContentAsString();
        Long reservationId = objectMapper.readTree(responseContent).get("id").asLong();

        Reservation reservation = reservationRepo.findById(reservationId).orElseThrow();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservation.getOutlet()).isNotNull();

        mockMvc.perform(post("/api/reservations/" + reservationId + "/confirm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ReservationStatus.CONFIRMED.name()));

        reservation = reservationRepo.findById(reservationId).orElseThrow();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        mockMvc.perform(post("/api/reservations/" + reservationId + "/cancel")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(ReservationStatus.CANCELLED.name()));

        reservation = reservationRepo.findById(reservationId).orElseThrow();
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }
    @AfterEach
    void tearDown() {
        reservationRepo.deleteAll();
        outletRepo.deleteAll();
        stationRepo.deleteAll();
        operatorRepo.deleteAll();
        consumerRepo.deleteAll();
    }
}
