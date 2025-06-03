package tqs.evsync.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tqs.evsync.backend.model.Consumer;
import tqs.evsync.backend.repository.ConsumerRepository;

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
}
