package tqs.evsync.backend.controller;

import java.util.List;
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


    @GetMapping("/consumer/{consumerId}")
    public ResponseEntity<?> getChargingSessionsByConsumer(@PathVariable Long consumerId) {
        List<ChargingSession> sessions = sessionService.getSessionsByConsumer(consumerId);
        if (sessions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhuma sessão encontrada para este consumidor.");
        }
        return ResponseEntity.ok(sessions);
    }

}
