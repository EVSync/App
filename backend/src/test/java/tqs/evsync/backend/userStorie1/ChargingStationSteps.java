package tqs.evsync.backend.userStorie1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ChargingStationSteps {
    
    @Autowired(required = false)
    private TestRestTemplate restTemplate;
    
    @Test
    void contextLoads() {
        assertNotNull(restTemplate, "Spring context should load with TestRestTemplate");
    }
}