package tqs.evsync.backend.controller;

import java.util.Optional;

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

    @GetMapping
    public ResponseEntity<?> getAllOperators() {
        return ResponseEntity.ok(operatorRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Operator> getOperatorById(@PathVariable Long id) {
        Optional<Operator> operator = operatorRepository.findById(id);
        return operator.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Operator> updateOperator(
            @PathVariable Long id,
            @RequestBody Operator operatorDetails) {
        return operatorRepository.findById(id)
                .map(operator -> {
                    operator.setEmail(operatorDetails.getEmail());
                    operator.setPassword(operatorDetails.getPassword());
                    operator.setOperatorType(operatorDetails.getOperatorType());
                    Operator updatedOperator = operatorRepository.save(operator);
                    return ResponseEntity.ok(updatedOperator);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOperator(@PathVariable Long id) {
        return operatorRepository.findById(id)
                .map(operator -> {
                    operatorRepository.delete(operator);
                    return ResponseEntity.ok().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Operator> getOperatorByEmail(@PathVariable String email) {
        Optional<Operator> operator = operatorRepository.findByEmail(email);
        return operator.map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/charging-stations")
    public ResponseEntity<?> getOperatorChargingStations(@PathVariable Long id) {
        return operatorRepository.findById(id)
                .map(operator -> ResponseEntity.ok(operator.getChargingStation()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
