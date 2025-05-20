package tqs.evsync.backend.userStorie1;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import io.cucumber.spring.CucumberContextConfiguration;
import tqs.evsync.backend.BackendApplication;

@CucumberContextConfiguration
@SpringBootTest(
    classes = BackendApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.initialization-mode=always"
    }
)
@ActiveProfiles("test")
public class CucumberTestConfig {
}