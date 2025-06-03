package tqs.evsync.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import tqs.evsync.backend.model.ChargingOutlet;
import tqs.evsync.backend.model.ChargingStation;
import tqs.evsync.backend.model.Consumer;
import tqs.evsync.backend.model.Operator;
import tqs.evsync.backend.model.Reservation;
import tqs.evsync.backend.model.enums.OperatorType;
import tqs.evsync.backend.model.enums.ReservationStatus;
import tqs.evsync.backend.repository.ChargingOutletRepository;
import tqs.evsync.backend.repository.ChargingStationRepository;
import tqs.evsync.backend.repository.ConsumerRepository;
import tqs.evsync.backend.repository.OperatorRepository;
import tqs.evsync.backend.repository.ReservationRepository;
import tqs.evsync.backend.repository.SessionRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SessionControllerIT {

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

    @Autowired
    private ReservationRepository reservationRepo;

    @Autowired
    private ConsumerRepository consumerRepo;

    @Autowired
    private SessionRepository sessionRepo;

    private Long outletId;

    private Long reservationId;

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
        outlet.setAvailable(true);
        outlet.setChargingStation(station);
        outlet = outletRepo.save(outlet);

        Consumer consumer = new Consumer();
        consumer.setEmail("test@example.com");
        consumer.setPassword("password");
        consumer.setWallet(100.0); 
        consumer = consumerRepo.save(consumer);

        Reservation reservation = new Reservation();
        reservation.setOutlet(outlet);
        reservation.setStation(station); 
        reservation.setConsumer(consumer); 
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setStartTime(LocalDateTime.now().plusHours(1).toString());
        reservation.setDuration(1.0); 
        reservation.setReservationFee(5.0); 

        consumer.setReservations(List.of(reservation));
        reservation = reservationRepo.save(reservation);
        reservationId = reservation.getId();
        outletId = outlet.getId();
    }

    @AfterEach
    void tearDown() {
        sessionRepo.deleteAll();
        reservationRepo.deleteAll();
        outletRepo.deleteAll();
        stationRepo.deleteAll();
        operatorRepo.deleteAll();
        consumerRepo.deleteAll();

        reservationId = null;
        outletId = null;
    }

    @Test
    void testCreateSessionIntegration() throws Exception {
        mockMvc.perform(post("/api/v1/sessions/start?reservationId=" + reservationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testGetAllSessions() throws Exception {
        mockMvc.perform(post("/api/v1/sessions/start?reservationId=" + reservationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/sessions"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
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
                        .contentType(MediaType.APPLICATION_JSON)
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
        MvcResult createResult = mockMvc.perform(post("/api/v1/sessions/start?reservationId=" + reservationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String response = createResult.getResponse().getContentAsString();
        Long sessionId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(put("/api/v1/sessions/" + sessionId + "/end")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "energyUsed", 15.0,
                                "totalCost", 7.5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
 
}