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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import tqs.evsync.backend.model.Operator;
import tqs.evsync.backend.model.enums.OperatorType;
import tqs.evsync.backend.repository.OperatorRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.profiles.active=test")
@ActiveProfiles("test")
public class OperatorControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OperatorRepository operatorRepository;

    private Long operatorId;

    @BeforeEach
    void setup() {
        Operator operator = new Operator();
        operator.setEmail("operator@example.com");
        operator.setPassword("securepassword");
        operator.setOperatorType(OperatorType.OPERATOR);
        operator = operatorRepository.save(operator);
        operatorId = operator.getId();
    }

    @AfterEach
    void tearDown() {
        operatorRepository.deleteAll();
        operatorId = null;
    }

    @Test
    void testCreateOperator_Success() throws Exception {
        Operator newOperator = new Operator();
        newOperator.setEmail("new@example.com");
        newOperator.setPassword("newpassword");
        newOperator.setOperatorType(OperatorType.ADMIN);

        mockMvc.perform(post("/api/operators")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newOperator)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.operatorType").value("ADMIN"));
    }

    @Test
    void testGetAllOperators() throws Exception {
        mockMvc.perform(get("/api/operators"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(operatorId))
                .andExpect(jsonPath("$[0].email").value("operator@example.com"))
                .andExpect(jsonPath("$[0].operatorType").value("OPERATOR"));
    }

    @Test
    void testGetOperatorById_Success() throws Exception {
        mockMvc.perform(get("/api/operators/{id}", operatorId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(operatorId))
                .andExpect(jsonPath("$.email").value("operator@example.com"));
    }

    @Test
    void testGetOperatorById_NotFound() throws Exception {
        mockMvc.perform(get("/api/operators/{id}", 999999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateOperator_Success() throws Exception {
        Operator updatedOperator = new Operator();
        updatedOperator.setEmail("updated@example.com");
        updatedOperator.setPassword("newpassword");
        updatedOperator.setOperatorType(OperatorType.ADMIN);

        mockMvc.perform(put("/api/operators/{id}", operatorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedOperator)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.operatorType").value("ADMIN"));
    }

    @Test
    void testDeleteOperator_Success() throws Exception {
        mockMvc.perform(delete("/api/operators/{id}", operatorId))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/operators/{id}", operatorId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetOperatorByEmail_Success() throws Exception {
        mockMvc.perform(get("/api/operators/email/{email}", "operator@example.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(operatorId))
                .andExpect(jsonPath("$.email").value("operator@example.com"));
    }

    @Test
    void testGetOperatorByEmail_NotFound() throws Exception {
        mockMvc.perform(get("/api/operators/email/{email}", "nonexistent@example.com"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetOperatorChargingStations_Success() throws Exception {
        mockMvc.perform(get("/api/operators/{id}/charging-stations", operatorId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetOperatorChargingStations_NotFound() throws Exception {
        mockMvc.perform(get("/api/operators/{id}/charging-stations", 999999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}