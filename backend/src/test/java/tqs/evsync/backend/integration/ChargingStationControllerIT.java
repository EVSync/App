package tqs.evsync.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import tqs.evsync.backend.model.Operator;
import tqs.evsync.backend.model.enums.ChargingStationStatus;
import tqs.evsync.backend.model.enums.OperatorType;
import tqs.evsync.backend.repository.ChargingOutletRepository;
import tqs.evsync.backend.repository.ChargingStationRepository;
import tqs.evsync.backend.repository.OperatorRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChargingStationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChargingStationRepository stationRepo;

    @Autowired
    private OperatorRepository operatorRepo;

    @Autowired
    private ChargingOutletRepository outletRepo;

    private Long stationId;
    private Long operatorId;
    private Long outletId;

    @BeforeEach
    void setup() {
        Operator operator = new Operator();
        operator.setEmail("station_test@example.com");
        operator.setPassword("1234");
        operator.setOperatorType(OperatorType.OPERATOR);
        operator = operatorRepo.save(operator);
        operatorId = operator.getId();

        ChargingStation station = new ChargingStation();
        station.setLatitude(40.63);
        station.setLongitude(-8.65);
        station.setOperator(operator);
        station.setStatus(ChargingStationStatus.AVAILABLE);
        station = stationRepo.save(station);
        stationId = station.getId();

        ChargingOutlet outlet = new ChargingOutlet();
        outlet.setMaxPower(22);
        outlet.setAvailable(true);
        outlet.setChargingStation(station);
        outlet = outletRepo.save(outlet);
        outletId = outlet.getId();
    }

    @AfterEach
    void tearDown() {
        outletRepo.deleteAll();
        stationRepo.deleteAll();
        operatorRepo.deleteAll();

        stationId = null;
        operatorId = null;
        outletId = null;
    }

    @Test
    void testGetAllChargingStations() throws Exception {
        mockMvc.perform(get("/charging-station"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(stationId))
                .andExpect(jsonPath("$[0].latitude").value(40.63));
    }

    @Test
    void testGetChargingStationById() throws Exception {
        mockMvc.perform(get("/charging-station/" + stationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(stationId))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void testGetChargingStationById_NotFound() throws Exception {
        mockMvc.perform(get("/charging-station/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetStationsNearby() throws Exception {
        mockMvc.perform(get("/charging-station/nearby/40.63/-8.65/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(stationId));
    }


    @Test
    void testGetAvailableStationsNearby() throws Exception {     
        mockMvc.perform(get("/charging-station/available-nearby/40.63/-8.65/10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(stationId))
            .andExpect(jsonPath("$[0].status").value("AVAILABLE"));
    }

    @Test
    void testGetStationsByOperator() throws Exception {
        mockMvc.perform(get("/charging-station/operator/" + operatorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(stationId));
    }

    @Test
    void testGetStationsByOperator_NotFound() throws Exception {
        mockMvc.perform(get("/charging-station/operator/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateChargingStationStatus() throws Exception {
        mockMvc.perform(put("/charging-station/" + stationId + "/status?status=MAINTENANCE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }

    @Test
    void testAddChargingOutlet() throws Exception {
        ChargingOutlet newOutlet = new ChargingOutlet();
        newOutlet.setMaxPower(11);
        newOutlet.setAvailable(true);
        ChargingStation station = stationRepo.findById(stationId).orElseThrow();
        station.addChargingOutlet(newOutlet);
        newOutlet.setChargingStation(station);

        mockMvc.perform(put("/charging-station/" + stationId + "/add-charging-outlet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newOutlet)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chargingOutlets.length()").value(2));
    }

    @Test
    void testRemoveChargingOutlet() throws Exception {
        ChargingOutlet outletToRemove = new ChargingOutlet();
        outletToRemove.setId(outletId);

        mockMvc.perform(put("/charging-station/" + stationId + "/remove-charging-outlet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(outletToRemove)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chargingOutlets.length()").value(0));
    }

    @Test
    void testAddChargingStation() throws Exception {
        ChargingStation newStation = new ChargingStation();
        newStation.setLatitude(41.15);
        newStation.setLongitude(-8.61);
        newStation.setStatus(ChargingStationStatus.AVAILABLE);

        Operator operator = new Operator();
        operator.setId(operatorId);
        newStation.setOperator(operator);

        MvcResult result = mockMvc.perform(post("/charging-station")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newStation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latitude").value(41.15))
                .andReturn();

        Long newStationId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(get("/charging-station/" + newStationId))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteChargingStation() throws Exception {
        mockMvc.perform(delete("/charging-station/" + stationId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/charging-station/" + stationId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteChargingStation_NotFound() throws Exception {
        mockMvc.perform(delete("/charging-station/999999"))
                .andExpect(status().isNotFound());
    }
}