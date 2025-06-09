package tqs.evsync.backend.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tqs.evsync.backend.model.ChargingSession;
import tqs.evsync.backend.model.Consumer;
import tqs.evsync.backend.repository.ConsumerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/consumers")
public class ConsumerController {

    @Autowired
    private ConsumerRepository consumerRepository;

    @PostMapping
    public ResponseEntity<Consumer> createConsumer(@RequestBody Consumer consumer) {
        Consumer savedConsumer = consumerRepository.save(consumer);
        return ResponseEntity.ok(savedConsumer);
    }

    @GetMapping
    public ResponseEntity<?> getAllConsumers() {
        return ResponseEntity.ok(consumerRepository.findAll());
    }

    @PostMapping("/signup")
    public ResponseEntity<Consumer> signup(@RequestBody Consumer incoming) {
        // you may want to hash the password!
        Consumer saved = consumerRepository.save(incoming);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<Consumer> login(@RequestBody Consumer creds) {
        return consumerRepository
            .findByEmail(creds.getEmail())
            .filter(c -> c.getPassword().equals(creds.getPassword()))
            .map(c -> ResponseEntity.ok(c))
            .orElse(ResponseEntity.status(401).build());
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

    @GetMapping("/{id}/reservations")
    public ResponseEntity<?> getReservationsByConsumerId(@PathVariable Long id) {
        Optional<Consumer> optionalConsumer = consumerRepository.findById(id);
        if (optionalConsumer.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Consumer consumer = optionalConsumer.get();
        return ResponseEntity.ok(consumer.getReservations());
    }


}
