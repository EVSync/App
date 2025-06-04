package tqs.evsync.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tqs.evsync.backend.model.ChargingSession;
import tqs.evsync.backend.model.Consumer;
import tqs.evsync.backend.repository.ConsumerRepository;
import tqs.evsync.backend.service.SessionService;

@RestController
@RequestMapping("/api/consumers")
public class ConsumerController {

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private SessionService sessionService;

    @PostMapping
    public ResponseEntity<Consumer> createConsumer(@RequestBody Consumer consumer) {
        Consumer savedConsumer = consumerRepository.save(consumer);
        return ResponseEntity.ok(savedConsumer);
    }

    @GetMapping
    public ResponseEntity<?> getAllConsumers() {
        return ResponseEntity.ok(consumerRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getConsumerById(@PathVariable Long id) {
        Optional<Consumer> consumer = consumerRepository.findById(id);
        return consumer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateConsumer(@PathVariable Long id, @RequestBody Consumer consumerDetails) {
        Optional<Consumer> optionalConsumer = consumerRepository.findById(id);
        if (optionalConsumer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Consumer consumer = optionalConsumer.get();
        consumer.setEmail(consumerDetails.getEmail());
        consumer.setPassword(consumerDetails.getPassword());
        consumer.setWallet(consumerDetails.getWallet());
        
        Consumer updatedConsumer = consumerRepository.save(consumer);
        return ResponseEntity.ok(updatedConsumer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConsumer(@PathVariable Long id) {
        Optional<Consumer> optionalConsumer = consumerRepository.findById(id);
        if (optionalConsumer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        consumerRepository.delete(optionalConsumer.get());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getConsumerByEmail(@PathVariable String email) {
        Optional<Consumer> consumer = consumerRepository.findByEmail(email);
        return consumer.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/wallet")
    public ResponseEntity<?> addToWallet(@PathVariable Long id, @RequestParam Double amount) {
        if (amount <= 0) {
            return ResponseEntity.badRequest().body("Valor deve ser positivo");
        }

        Optional<Consumer> optionalConsumer = consumerRepository.findById(id);
        if (optionalConsumer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Consumer consumer = optionalConsumer.get();
        consumer.setWallet(consumer.getWallet() + amount);
        consumerRepository.save(consumer);
        return ResponseEntity.ok(consumer);
    }


    @GetMapping("/{id}/wallet")
    public ResponseEntity<?> getWalletBalance(@PathVariable Long id) {
        Optional<Consumer> optionalConsumer = consumerRepository.findById(id);
        if (optionalConsumer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(optionalConsumer.get().getWallet());
    }

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<?> getConsumptionDashboard(@PathVariable Long id) {
        Optional<Consumer> optionalConsumer = consumerRepository.findById(id);
        if (optionalConsumer.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Consumidor não encontrado.");
        }

        Consumer consumer = optionalConsumer.get();
        List<ChargingSession> sessions = sessionService.getSessionsByConsumer(consumer);

        double totalEnergy = sessions.stream().mapToDouble(ChargingSession::getEnergyConsumed).sum();
        double totalCost = sessions.stream().mapToDouble(ChargingSession::getTotalCost).sum();

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("consumerId", consumer.getId());
        dashboard.put("email", consumer.getEmail());
        dashboard.put("totalSessions", sessions.size());
        dashboard.put("totalEnergyConsumed", totalEnergy);
        dashboard.put("totalCost", totalCost);
        dashboard.put("wallet", consumer.getWallet());

        return ResponseEntity.ok(dashboard);
    }


}
