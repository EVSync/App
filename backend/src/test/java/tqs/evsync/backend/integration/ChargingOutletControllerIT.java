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
import tqs.evsync.backend.model.enums.OperatorType;
import tqs.evsync.backend.repository.ChargingOutletRepository;
import tqs.evsync.backend.repository.ChargingStationRepository;
import tqs.evsync.backend.repository.OperatorRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ChargingOutletControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChargingOutletRepository outletRepository;

    @Autowired
    private ChargingStationRepository stationRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    private Long stationId;
    private Long outletId;

    @BeforeEach
    void setup() {
        Operator operator = new Operator();
        operator.setEmail("outlet_test@example.com");
        operator.setPassword("1234");
        operator.setOperatorType(OperatorType.OPERATOR);
        operator = operatorRepository.save(operator);

        ChargingStation station = new ChargingStation();
        station.setLatitude(40.63);
        station.setLongitude(-8.65);
        station.setOperator(operator);
        station = stationRepository.save(station);
        stationId = station.getId();

        ChargingOutlet outlet = new ChargingOutlet();
        outlet.setMaxPower(22);
        outlet.setAvailable(true);
        outlet.setChargingStation(station);
        station.addChargingOutlet(outlet);
        outlet = outletRepository.save(outlet);
        outletId = outlet.getId();
        
    }

    @AfterEach
    void tearDown() {
        outletRepository.deleteAll();
        stationRepository.deleteAll();
        operatorRepository.deleteAll();

        stationId = null;
        outletId = null;
    }

    @Test
    void testCreateOutlet_Success() throws Exception {
        ChargingOutlet newOutlet = new ChargingOutlet();
        newOutlet.setMaxPower(11);
        newOutlet.setAvailable(true);
        
        ChargingStation station = new ChargingStation();
        station.setId(stationId);
        newOutlet.setChargingStation(station);

        mockMvc.perform(post("/api/outlets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newOutlet)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.maxPower").value(11));
    }

    @Test
    void testCreateOutlet_InvalidStation() throws Exception {
        ChargingOutlet newOutlet = new ChargingOutlet();
        newOutlet.setMaxPower(11);
        newOutlet.setAvailable(true);
        
        ChargingStation station = new ChargingStation();
        station.setId(999999L);
        newOutlet.setChargingStation(station);

        mockMvc.perform(post("/api/outlets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newOutlet)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testListOutlets() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/outlets"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();
        
        System.out.println("DEBUG: Endpoint response: " + 
                        result.getResponse().getContentAsString());
                        
        mockMvc.perform(get("/api/outlets"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(outletId))
                .andExpect(jsonPath("$[0].maxPower").value(22));
    }

    @Test
    void testListOutlets_Empty() throws Exception {
        outletRepository.deleteAll();

        mockMvc.perform(get("/api/outlets"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}