package tqs.evsync.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tqs.evsync.backend.model.Operator;
import tqs.evsync.backend.repository.OperatorRepository;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/operators")
public class OperatorController {

    @Autowired
    private OperatorRepository operatorRepository;

    @PostMapping
    public ResponseEntity<Operator> createOperator(@RequestBody Operator operator) {
        Operator savedOperator = operatorRepository.save(operator);
        return ResponseEntity.ok(savedOperator);
    }
}
