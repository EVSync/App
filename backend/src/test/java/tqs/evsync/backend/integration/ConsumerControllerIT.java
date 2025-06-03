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

import com.fasterxml.jackson.databind.ObjectMapper;

import tqs.evsync.backend.model.Consumer;
import tqs.evsync.backend.repository.ConsumerRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ConsumerControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConsumerRepository consumerRepository;

    private Long consumerId;

    @BeforeEach
    void setup() {
        Consumer consumer = new Consumer();
        consumer.setEmail("test@example.com");
        consumer.setPassword("password123");
        consumer.setWallet(100.0);
        consumer = consumerRepository.save(consumer);
        consumerId = consumer.getId();
    }

    @AfterEach
    void tearDown() {
        consumerRepository.deleteAll();
        consumerId = null;
    }

    @Test
    void testCreateConsumer_Success() throws Exception {
        Consumer newConsumer = new Consumer();
        newConsumer.setEmail("new@example.com");
        newConsumer.setPassword("newpassword");
        newConsumer.setWallet(50.0);

        mockMvc.perform(post("/api/consumers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newConsumer)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.wallet").value(50.0));
    }

    @Test
    void testGetAllConsumers() throws Exception {
        mockMvc.perform(get("/api/consumers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(consumerId))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[0].wallet").value(100.0));
    }

    @Test
    void testGetConsumerById_Success() throws Exception {
        mockMvc.perform(get("/api/consumers/{id}", consumerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(consumerId))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testGetConsumerById_NotFound() throws Exception {
        mockMvc.perform(get("/api/consumers/{id}", 999999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateConsumer_Success() throws Exception {
        Consumer updatedConsumer = new Consumer();
        updatedConsumer.setEmail("updated@example.com");
        updatedConsumer.setPassword("newpassword");
        updatedConsumer.setWallet(200.0);

        mockMvc.perform(put("/api/consumers/{id}", consumerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedConsumer)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.wallet").value(200.0));
    }

    @Test
    void testDeleteConsumer_Success() throws Exception {
        mockMvc.perform(delete("/api/consumers/{id}", consumerId))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/consumers/{id}", consumerId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetConsumerByEmail_Success() throws Exception {
        mockMvc.perform(get("/api/consumers/email/{email}", "test@example.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(consumerId))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testAddToWallet_Success() throws Exception {
        mockMvc.perform(put("/api/consumers/{id}/wallet?amount=50.0", consumerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wallet").value(150.0));
    }

    @Test
    void testAddToWallet_InvalidAmount() throws Exception {
        mockMvc.perform(put("/api/consumers/{id}/wallet?amount=-10.0", consumerId))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetWalletBalance_Success() throws Exception {
        mockMvc.perform(get("/api/consumers/{id}/wallet", consumerId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("100.0"));
    }
}